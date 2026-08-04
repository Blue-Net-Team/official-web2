package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.githuborg.GitHubOrgInvitationAdminResult;
import com.bluenet.web.application.service.GitHubOrgInvitationAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.GitHubOrgInvitationResult;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.GitHubOrgInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * GitHub 组织邀请管理应用服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOrgInvitationAdminAppServiceImpl implements GitHubOrgInvitationAdminAppService {

    private final UserRepository userRepository;
    private final GitHubOrgInvitationService gitHubOrgInvitationService;

    @Override
    public GitHubOrgInvitationAdminResult.Detail inviteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在，ID: " + userId));
        GitHubOrgInvitationResult result = gitHubOrgInvitationService.invite(user);
        return new GitHubOrgInvitationAdminResult.Detail(userId, result.success(), result.reason());
    }

    @Override
    public GitHubOrgInvitationAdminResult.Batch inviteBatch(List<Long> userIds) {
        List<GitHubOrgInvitationAdminResult.Detail> details = new ArrayList<>();
        int succeeded = 0;
        for (Long userId : userIds) {
            GitHubOrgInvitationAdminResult.Detail detail = inviteOneSafely(userId);
            details.add(detail);
            if (detail.success()) {
                succeeded++;
            }
        }
        return new GitHubOrgInvitationAdminResult.Batch(
                userIds.size(), succeeded, userIds.size() - succeeded, details);
    }

    /**
     * 邀请单个用户，所有异常转换为失败结果，不中断批量流程。
     */
    private GitHubOrgInvitationAdminResult.Detail inviteOneSafely(Long userId) {
        try {
            return inviteUser(userId);
        } catch (DataNotFound e) {
            return new GitHubOrgInvitationAdminResult.Detail(userId, false, "用户不存在");
        } catch (Exception e) {
            log.error("Failed to invite user {} to GitHub org", userId, e);
            return new GitHubOrgInvitationAdminResult.Detail(userId, false, "邀请失败: " + e.getMessage());
        }
    }
}
