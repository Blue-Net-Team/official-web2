package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubIssueClient {

    private static final String GITHUB_CREATE_ISSUE_URL_TEMPLATE = "%s/repos/%s/%s/issues";
    private static final String GITHUB_LIST_ISSUES_URL_TEMPLATE = "%s/repos/%s/%s/issues?state=all&sort=updated&direction=desc&per_page=%d&page=%d&since=%s";
    private static final int PER_PAGE = 100;
    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

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

    /**
     * 查询指定仓库中最近更新的 Issue 列表。
     *
     * @param since
     *            只返回该时间之后有更新的 Issue（UTC）
     * @return Issue 列表，按更新时间降序排列
     */
    public List<GitHubIssueListResult> listIssues(Instant since) {
        String accessToken = tokenService.getInstallationAccessToken();
        RestTemplate restTemplate = createRestTemplate();

        String sinceParam = ISO_INSTANT_FORMATTER.format(since);
        List<GitHubIssueListResult> allResults = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = String.format(
                    GITHUB_LIST_ISSUES_URL_TEMPLATE,
                    properties.getApiBaseUrl(),
                    properties.getOwner(),
                    properties.getRepo(),
                    PER_PAGE,
                    page,
                    sinceParam);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "BlueNet-Bug-Sync");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException(
                        "GitHub API error: failed to list issues, status=" + response.getStatusCode());
            }

            List<GitHubIssueListResult> pageResults = parseIssueList(response.getBody());
            if (pageResults.isEmpty()) {
                break;
            }
            allResults.addAll(pageResults);
            page++;
        }

        log.info("GitHub Issue 列表查询完成: since={}, total={}", sinceParam, allResults.size());
        return allResults;
    }

    private List<GitHubIssueListResult> parseIssueList(String jsonBody) {
        try {
            List<Map<String, Object>> items = objectMapper
                    .readValue(jsonBody, new TypeReference<List<Map<String, Object>>>() {
                    });
            List<GitHubIssueListResult> results = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Integer number = (Integer) item.get("number");
                String title = (String) item.get("title");
                String body = (String) item.get("body");
                String state = (String) item.get("state");
                String htmlUrl = (String) item.get("html_url");
                results.add(new GitHubIssueListResult(number, title, body, state, htmlUrl));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub issue list response", e);
        }
    }

    RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
