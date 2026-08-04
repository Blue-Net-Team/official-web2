package com.bluenet.web.infrastructure.github;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.GitHubOrgInvitationResult;
import com.bluenet.web.domain.service.GitHubOrgInvitationService;
import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * GitHub 组织邀请领域服务实现。
 * <p>
 * 编排邀请逻辑：解析邀请身份（githubId 优先，email 兜底）-> 按方向解析 team ID -> 调用邀请
 * API。所有异常在内部捕获并转换为结果或日志，不向外抛出。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOrgInvitationServiceImpl implements GitHubOrgInvitationService {

    private final GitHubAppsProperties appsProperties;
    private final GitHubOrgInvitationClient invitationClient;
    private final GitHubOrgTeamResolver teamResolver;

    @Override
    public GitHubOrgInvitationResult invite(User user) {
        GitHubAppConfig config = appsProperties.findApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME);
        if (config == null || !config.isEnabled()) {
            log.info("GitHub org invitation is not enabled, skip inviting user {}", user.getId());
            return GitHubOrgInvitationResult.failure("GitHub 组织邀请功能未启用");
        }

        try {
            // 1. 解析邀请身份：githubId（invitee_id）优先，email 兜底
            Long inviteeId = parseInviteeId(user);
            String email = null;
            if (inviteeId == null) {
                email = user.getEmail();
                if (email == null || email.isBlank()) {
                    log.warn(
                            "Cannot invite user {} to GitHub org: no github binding and no email",
                            user.getId());
                    return GitHubOrgInvitationResult.failure("未绑定 GitHub 且无邮箱，无法邀请");
                }
            }

            // 2. 按方向解析 team ID；解析失败只记录日志，仍发送组织邀请
            List<Long> teamIds = resolveTeamIds(config, user);

            // 3. 调用邀请 API
            GitHubOrgInvitationStatus status = invitationClient.createInvitation(inviteeId, email, teamIds);

            String identity = inviteeId != null ? "githubId=" + inviteeId : "email=" + email;
            if (status == GitHubOrgInvitationStatus.ALREADY_EXISTS) {
                log.info(
                        "GitHub org invitation skipped for user {} ({}): already member or invited",
                        user.getId(),
                        identity);
                return GitHubOrgInvitationResult.success("用户已在组织中或已被邀请");
            }
            log.info("GitHub org invitation sent for user {} ({}), teams={}", user.getId(), identity, teamIds);
            String teamInfo = teamIds.isEmpty() ? "" : "并分配至方向团队";
            return GitHubOrgInvitationResult.success("邀请已发送" + teamInfo);
        } catch (Exception e) {
            log.error("Failed to invite user {} to GitHub org", user.getId(), e);
            return GitHubOrgInvitationResult.failure("GitHub API 调用失败: " + e.getMessage());
        }
    }

    @Async("githubOrgExecutor")
    @Override
    public void inviteAsync(User user) {
        try {
            invite(user);
        } catch (Exception e) {
            // 兜底保护：异步邀请失败绝不影响主流程
            log.error("Async GitHub org invitation failed for user {}", user.getId(), e);
        }
    }

    /**
     * 解析用户的 GitHub 数字 ID。未绑定或格式非法时返回 null。
     */
    private Long parseInviteeId(User user) {
        String githubId = user.getGithubId();
        if (githubId == null || githubId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(githubId.trim());
        } catch (NumberFormatException e) {
            log.warn("User {} has non-numeric githubId {}, fallback to email", user.getId(), githubId);
            return null;
        }
    }

    /**
     * 按用户方向解析要加入的 team ID 列表。方向无映射或解析失败时返回空列表。
     */
    private List<Long> resolveTeamIds(GitHubAppConfig config, User user) {
        Direction direction = user.getDirection();
        if (direction == null) {
            return List.of();
        }
        String teamName = config.getTeamMapping().get(direction.name());
        if (teamName == null || teamName.isBlank()) {
            return List.of();
        }
        try {
            Optional<Long> teamId = teamResolver.resolveTeamId(teamName);
            if (teamId.isPresent()) {
                return List.of(teamId.get());
            }
            log.warn("GitHub team not found: name={}, direction={}", teamName, direction.name());
        } catch (Exception e) {
            log.warn("Failed to resolve GitHub team id: name={}", teamName, e);
        }
        return List.of();
    }
}
