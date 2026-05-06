package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubIssueClient {

    private static final String GITHUB_CREATE_ISSUE_URL_TEMPLATE = "%s/repos/%s/%s/issues";

    private final GitHubAppProperties properties;
    private final GitHubAppTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在配置的 GitHub 仓库中创建 Issue。
     *
     * @param title
     *            Issue 标题
     * @param body
     *            Issue Body（Markdown 格式）
     * @return 创建结果，包含 number 和 html_url
     */
    public GitHubIssueCreateResult createIssue(String title, String body) {
        String accessToken = tokenService.getInstallationAccessToken();
        RestTemplate restTemplate = createRestTemplate();

        String url = String.format(
                GITHUB_CREATE_ISSUE_URL_TEMPLATE,
                properties.getApiBaseUrl(),
                properties.getOwner(),
                properties.getRepo());

        Map<String, Object> requestBody = Map.of(
                "title",
                title,
                "body",
                body,
                "labels",
                List.of("Bug"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Bug-Sync");

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
                throw new RuntimeException(
                        "GitHub API error: failed to create issue, status=" + response.getStatusCode());
            }

            Map<String, Object> result = objectMapper
                    .readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
                    });
            Integer number = (Integer) result.get("number");
            String htmlUrl = (String) result.get("html_url");
            String resultTitle = (String) result.get("title");
            return new GitHubIssueCreateResult(number, htmlUrl, resultTitle);
        } catch (RuntimeException e) {
            log.error("Failed to create GitHub issue: title={}", title, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to create GitHub issue: title={}", title, e);
            throw new RuntimeException("Failed to create GitHub issue", e);
        }
    }

    RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
