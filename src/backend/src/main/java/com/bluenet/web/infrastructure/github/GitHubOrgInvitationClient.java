package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub 组织邀请 API 客户端。
 * <p>
 * 封装 {@code POST /orgs/{org}/invitations}，支持通过 invitee_id（GitHub 用户数字 ID）或
 * email 邀请，可同时指定要加入的 team。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubOrgInvitationClient {

    private static final String INVITATIONS_URL_TEMPLATE = "%s/orgs/%s/invitations";

    private final GitHubAppsProperties appsProperties;
    private final GitHubAppTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送组织邀请。
     *
     * @param inviteeId
     *            GitHub 用户数字 ID（与 email 二选一，优先使用）
     * @param email
     *            受邀者邮箱（inviteeId 为空时使用）
     * @param teamIds
     *            要加入的 team ID 列表，可为空
     * @return 邀请状态（SENT 或 ALREADY_EXISTS）
     */
    public GitHubOrgInvitationStatus createInvitation(Long inviteeId, String email, List<Long> teamIds) {
        GitHubAppConfig config = appsProperties.getApp(GitHubAppsProperties.ORG_INVITATION_APP_NAME);
        String accessToken = tokenService.getAccessToken(GitHubAppsProperties.ORG_INVITATION_APP_NAME);

        String url = String.format(INVITATIONS_URL_TEMPLATE, config.getApiBaseUrl(), config.getOrg());

        Map<String, Object> requestBody = new HashMap<>();
        if (inviteeId != null) {
            requestBody.put("invitee_id", inviteeId);
        } else {
            requestBody.put("email", email);
        }
        if (teamIds != null && !teamIds.isEmpty()) {
            requestBody.put("team_ids", teamIds);
        }

        HttpHeaders headers = createHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = createRestTemplate()
                    .exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException(
                        "GitHub API error: failed to create org invitation, status=" + response.getStatusCode());
            }
            return GitHubOrgInvitationStatus.SENT;
        } catch (HttpClientErrorException.UnprocessableEntity e) {
            // 422：用户已是组织成员或已被邀请，不视为错误
            log.info("GitHub org invitation already exists: inviteeId={}, email={}", inviteeId, email);
            return GitHubOrgInvitationStatus.ALREADY_EXISTS;
        } catch (RuntimeException e) {
            log.error("Failed to create GitHub org invitation: inviteeId={}, email={}", inviteeId, email, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to create GitHub org invitation: inviteeId={}, email={}", inviteeId, email, e);
            throw new RuntimeException("Failed to create GitHub org invitation", e);
        }
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Backend");
        return headers;
    }

    RestTemplate createRestTemplate() {
        return restTemplate;
    }
}
