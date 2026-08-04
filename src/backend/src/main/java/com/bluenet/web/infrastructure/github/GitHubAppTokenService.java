package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppConfig;
import com.bluenet.web.infrastructure.config.GitHubAppType;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.util.Map;

/**
 * GitHub App Installation Access Token 服务，所有 GitHub App 共享。
 * <p>
 * 按 App 名称获取 Token：使用共享的 JWT 生成逻辑，根据 App 配置的安装类型（repository /
 * organization）选择对应的 Installation 查询路径。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAppTokenService {

    private static final String REPO_INSTALLATION_URL_TEMPLATE = "%s/repos/%s/%s/installation";
    private static final String ORG_INSTALLATION_URL_TEMPLATE = "%s/orgs/%s/installation";
    private static final String GITHUB_ACCESS_TOKEN_URL_TEMPLATE = "%s/app/installations/%s/access_tokens";

    private final GitHubAppsProperties appsProperties;
    private final GitHubJwtGenerator jwtGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取指定 GitHub App 的 Installation Access Token。
     * <p>
     * 流程： 1. 使用私钥生成 JWT 2. 通过 JWT 查询安装目标（仓库或组织）对应的 installation id 3. 通过 JWT +
     * installation id 换取 Access Token
     *
     * @param appName
     *            App 配置名称（如 issue-sync、org-invitation）
     * @return Installation Access Token
     */
    public String getAccessToken(String appName) {
        GitHubAppConfig config = appsProperties.getApp(appName);
        if (!config.isEnabled()) {
            throw new IllegalStateException("GitHub App is not enabled: " + appName);
        }

        PrivateKey privateKey = jwtGenerator.loadPrivateKey(config.getPrivateKeyPath());
        String jwt = jwtGenerator.generateJwt(config.getAppId(), privateKey);
        RestTemplate restTemplate = createRestTemplate();

        try {
            // 1. 获取 installation id
            long installationId = fetchInstallationId(restTemplate, jwt, config);

            // 2. 换取 access token
            return fetchAccessToken(restTemplate, jwt, config.getApiBaseUrl(), installationId);
        } catch (RuntimeException e) {
            log.error("Failed to get GitHub installation access token: app={}", appName, e);
            throw e;
        }
    }

    private long fetchInstallationId(RestTemplate restTemplate, String jwt, GitHubAppConfig config) {
        String url = buildInstallationUrl(config);

        HttpEntity<Void> request = new HttpEntity<>(createJwtHeaders(jwt));
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException(
                    "GitHub API error: failed to fetch installation info, status=" + response.getStatusCode());
        }

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            Number id = (Number) body.get("id");
            if (id == null) {
                throw new RuntimeException("GitHub API response missing installation id");
            }
            return id.longValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub installation response", e);
        }
    }

    /**
     * 根据 App 安装类型构建 Installation 查询 URL。
     */
    private String buildInstallationUrl(GitHubAppConfig config) {
        GitHubAppType type = config.getType() == null ? GitHubAppType.REPOSITORY : config.getType();
        return switch (type) {
            case REPOSITORY -> String.format(
                    REPO_INSTALLATION_URL_TEMPLATE,
                    config.getApiBaseUrl(),
                    config.getOwner(),
                    config.getRepo());
            case ORGANIZATION -> String.format(
                    ORG_INSTALLATION_URL_TEMPLATE,
                    config.getApiBaseUrl(),
                    config.getOrg());
        };
    }

    private String fetchAccessToken(RestTemplate restTemplate, String jwt, String apiBaseUrl,
            long installationId) {
        String url = String.format(GITHUB_ACCESS_TOKEN_URL_TEMPLATE, apiBaseUrl, installationId);

        HttpEntity<Void> request = new HttpEntity<>(createJwtHeaders(jwt));
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
            throw new RuntimeException(
                    "GitHub API error: failed to fetch access token, status=" + response.getStatusCode());
        }

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            String token = (String) body.get("token");
            if (token == null || token.isBlank()) {
                throw new RuntimeException("GitHub API response missing access token");
            }
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub access token response", e);
        }
    }

    private HttpHeaders createJwtHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Backend");
        return headers;
    }

    RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
