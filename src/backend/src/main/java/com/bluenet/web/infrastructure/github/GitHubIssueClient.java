package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.bluenet.web.infrastructure.config.GitHubAppsProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubIssueClient {

    private static final String GITHUB_CREATE_ISSUE_URL_TEMPLATE = "%s/repos/%s/%s/issues";
    private static final String GITHUB_LIST_ISSUES_URL_TEMPLATE = "%s/repos/%s/%s/issues?state=all&sort=updated&direction=desc&per_page=%d&page=%d&since=%s";
    private static final int PER_PAGE = 100;
    private static final int MAX_PAGES = 10;
    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final GitHubAppProperties properties;
    private final GitHubAppTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

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
        String accessToken = tokenService.getAccessToken(GitHubAppsProperties.ISSUE_SYNC_APP_NAME);
        RestTemplate restTemplate = createRestTemplate();

        String url = String.format(
                GITHUB_CREATE_ISSUE_URL_TEMPLATE,
                properties.getApiBaseUrl(),
                properties.getOwner(),
                properties.getRepo());

        java.util.Map<String, Object> requestBody = java.util.Map.of(
                "title",
                title,
                "body",
                body,
                "labels",
                List.of("Bug"));

        HttpHeaders headers = createHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() != HttpStatus.CREATED || response.getBody() == null) {
                throw new RuntimeException(
                        "GitHub API error: failed to create issue, status=" + response.getStatusCode());
            }

            GitHubIssueCreateResponse result = objectMapper
                    .readValue(response.getBody(), GitHubIssueCreateResponse.class);
            return new GitHubIssueCreateResult(result.number(), result.htmlUrl(), result.title());
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
     * @return Issue 列表，按更新时间降序排列（不包含 Pull Request）
     */
    public List<GitHubIssueListResult> listIssues(Instant since) {
        String accessToken = tokenService.getAccessToken(GitHubAppsProperties.ISSUE_SYNC_APP_NAME);
        RestTemplate restTemplate = createRestTemplate();

        String sinceParam = ISO_INSTANT_FORMATTER.format(since);
        List<GitHubIssueListResult> allResults = new ArrayList<>();
        int page = 1;

        HttpHeaders headers = createHeaders(accessToken);

        while (page <= MAX_PAGES) {
            String url = String.format(
                    GITHUB_LIST_ISSUES_URL_TEMPLATE,
                    properties.getApiBaseUrl(),
                    properties.getOwner(),
                    properties.getRepo(),
                    PER_PAGE,
                    page,
                    sinceParam);

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

        if (page > MAX_PAGES) {
            log.warn("GitHub Issue 列表查询达到最大页数限制: maxPages={}, since={}", MAX_PAGES, sinceParam);
        }

        log.info("GitHub Issue 列表查询完成: since={}, total={}", sinceParam, allResults.size());
        return allResults;
    }

    private List<GitHubIssueListResult> parseIssueList(String jsonBody) {
        try {
            List<GitHubIssueRaw> items = objectMapper
                    .readValue(jsonBody, new TypeReference<List<GitHubIssueRaw>>() {
                    });
            List<GitHubIssueListResult> results = new ArrayList<>();
            for (GitHubIssueRaw item : items) {
                // 过滤 Pull Request：GitHub /issues API 同时返回 Issues 和 PRs
                if (item.pullRequest() != null) {
                    continue;
                }
                results.add(
                        new GitHubIssueListResult(
                                toInteger(item.number()),
                                item.title(),
                                item.body(),
                                item.state(),
                                item.htmlUrl()));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub issue list response", e);
        }
    }

    /**
     * 将 Number 安全转换为 Integer。
     * <p>
     * Jackson 可能将 JSON 数字解析为 Long 或 Integer，此方法统一处理。
     * </p>
     *
     * @param number
     *            原始数字
     * @return Integer 值，null 输入返回 null
     * @throws IllegalArgumentException
     *             超出 Integer 范围时抛出
     */
    private Integer toInteger(Number number) {
        if (number == null) {
            return null;
        }
        long value = number.longValue();
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Issue number out of Integer range: " + value);
        }
        return number.intValue();
    }

    /**
     * 创建共享的 RestTemplate 实例。
     * <p>
     * RestTemplate 是线程安全的，复用同一实例可减少 GC 压力。
     * </p>
     */
    RestTemplate createRestTemplate() {
        return restTemplate;
    }

    /**
     * 创建公共的 HTTP Headers。
     *
     * @param accessToken
     *            GitHub 访问令牌
     * @return 已配置认证和 Accept 的 Headers
     */
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "BlueNet-Bug-Sync");
        return headers;
    }

    /**
     * GitHub Issue 列表项原始响应结构（用于反序列化）。
     */
    private record GitHubIssueRaw(
            @JsonProperty("number") Number number,
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("state") String state,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("pull_request") Object pullRequest) {
    }

    /**
     * GitHub Issue 创建响应结构（用于反序列化）。
     */
    private record GitHubIssueCreateResponse(
            @JsonProperty("number") Integer number,
            @JsonProperty("html_url") String htmlUrl,
            @JsonProperty("title") String title) {
    }
}
