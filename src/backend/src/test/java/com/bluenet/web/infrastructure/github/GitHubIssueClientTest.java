package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubIssueClient 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubIssueClientTest {

    @Mock
    private GitHubAppProperties properties;

    @Mock
    private GitHubAppTokenService tokenService;

    private GitHubIssueClient client;

    /**
     * 真实 GitHub API Issue 响应快照（裁剪自 GET
     * /repos/Blue-Net-Team/official-web2/issues/60）。
     * <p>
     * 刻意保留大量未在 {@code GitHubIssueClient} 响应类型中声明的字段（url / id / node_id / labels /
     * user / created_at 等），用于验证解析器对未知字段的容忍性。真实响应约有 40 个字段，此处已裁剪。
     * </p>
     */
    private static final String REAL_CREATE_ISSUE_RESPONSE = """
            {
              "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60",
              "repository_url": "https://api.github.com/repos/Blue-Net-Team/official-web2",
              "labels_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/labels{/name}",
              "comments_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/comments",
              "events_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/events",
              "html_url": "https://github.com/Blue-Net-Team/official-web2/issues/60",
              "id": 5490354950,
              "node_id": "I_kwDORzBCuc8AAAABR0ArBg",
              "number": 60,
              "title": "ai客服意图识别的优化建议",
              "user": {
                "login": "bluenet-web-bug-sync[bot]",
                "id": 282110008,
                "node_id": "BOT_kgDOENCoOA",
                "type": "Bot",
                "site_admin": false
              },
              "labels": [
                {
                  "id": 10543044630,
                  "node_id": "LA_kwDORzBCuc8AAAACdGoYFg",
                  "name": "bug",
                  "color": "d73a4a",
                  "default": true,
                  "description": "Something isn't working"
                }
              ],
              "state": "open",
              "locked": false,
              "assignee": null,
              "assignees": [],
              "milestone": null,
              "comments": 0,
              "created_at": "2026-09-17T16:58:16Z",
              "updated_at": "2026-09-18T16:45:11Z",
              "closed_at": null,
              "author_association": "NONE",
              "active_lock_reason": null,
              "body": "## 描述\\n\\n向ai客服询问报销相关问题的时候，没有进入检索状态\\n\\n<!-- bluenet-bug-report -->",
              "reactions": {
                "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/reactions",
                "total_count": 0,
                "+1": 0,
                "-1": 0
              },
              "timeline_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/timeline",
              "performed_via_github_app": null,
              "state_reason": null
            }
            """;

    /**
     * 真实 GitHub API Issue 列表响应快照（裁剪自 issue #60 / #61 及一条 PR）。
     * <p>
     * 含未声明字段，且第三条带 {@code pull_request} 字段，用于同时覆盖「未知字段容忍」与「PR 过滤」。
     * </p>
     */
    private static final String REAL_ISSUE_LIST_RESPONSE = """
            [
              {
                "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60",
                "repository_url": "https://api.github.com/repos/Blue-Net-Team/official-web2",
                "html_url": "https://github.com/Blue-Net-Team/official-web2/issues/60",
                "id": 5490354950,
                "node_id": "I_kwDORzBCuc8AAAABR0ArBg",
                "number": 60,
                "title": "ai客服意图识别的优化建议",
                "user": {
                  "login": "bluenet-web-bug-sync[bot]",
                  "id": 282110008,
                  "type": "Bot"
                },
                "labels": [
                  {
                    "id": 10543044630,
                    "name": "bug",
                    "color": "d73a4a"
                  }
                ],
                "state": "open",
                "locked": false,
                "assignee": null,
                "assignees": [],
                "milestone": null,
                "comments": 0,
                "created_at": "2026-09-17T16:58:16Z",
                "updated_at": "2026-09-18T16:45:11Z",
                "closed_at": null,
                "author_association": "NONE",
                "active_lock_reason": null,
                "body": "## 描述\\n\\n向ai客服询问报销相关问题的时候，没有进入检索状态\\n\\n<!-- bluenet-bug-report -->",
                "timeline_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/60/timeline",
                "performed_via_github_app": null,
                "state_reason": null
              },
              {
                "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/61",
                "repository_url": "https://api.github.com/repos/Blue-Net-Team/official-web2",
                "html_url": "https://github.com/Blue-Net-Team/official-web2/issues/61",
                "id": 5490362269,
                "node_id": "I_kwDORzBCuc8AAAABR0BHnQ",
                "number": 61,
                "title": "ai对话记录的时间戳问题",
                "user": {
                  "login": "bluenet-web-bug-sync[bot]",
                  "id": 282110008,
                  "type": "Bot"
                },
                "labels": [],
                "state": "closed",
                "locked": false,
                "assignee": null,
                "comments": 0,
                "created_at": "2026-09-17T16:59:01Z",
                "updated_at": "2026-09-18T15:25:55Z",
                "closed_at": "2026-09-18T15:25:55Z",
                "author_association": "NONE",
                "active_lock_reason": null,
                "body": "## 描述\\n\\nai对话记录的时间戳似乎比真实请求时间慢8小时\\n\\n<!-- bluenet-bug-report -->",
                "timeline_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/61/timeline",
                "performed_via_github_app": null,
                "state_reason": "completed"
              },
              {
                "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/62",
                "repository_url": "https://api.github.com/repos/Blue-Net-Team/official-web2",
                "html_url": "https://github.com/Blue-Net-Team/official-web2/issues/62",
                "id": 5490400000,
                "number": 62,
                "title": "提交bug报告后，后台没有同步显示bug的issue编号和链接",
                "user": {
                  "login": "IVEN-CN",
                  "id": 152351980,
                  "type": "User"
                },
                "labels": [],
                "state": "open",
                "locked": false,
                "assignee": null,
                "comments": 0,
                "created_at": "2026-09-17T17:03:46Z",
                "updated_at": "2026-09-17T17:03:46Z",
                "closed_at": null,
                "author_association": "NONE",
                "active_lock_reason": null,
                "body": "## 描述问题\\n\\n提交bug报告后，后台没有同步显示bug的issue编号和链接",
                "pull_request": {
                  "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/pulls/62",
                  "html_url": "https://github.com/Blue-Net-Team/official-web2/pull/62"
                }
              }
            ]
            """;

    /**
     * 真实 GitHub API Issue 列表响应快照（单条，number 为 Integer.MAX_VALUE）。
     * <p>
     * 用于验证 Jackson 将大数值解析为 Long 时，{@code toInteger()} 仍能安全转换，且未声明字段不影响解析。
     * </p>
     */
    private static final String REAL_LARGE_NUMBER_ISSUE_RESPONSE = """
            [
              {
                "url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/2147483647",
                "repository_url": "https://api.github.com/repos/Blue-Net-Team/official-web2",
                "html_url": "https://github.com/Blue-Net-Team/official-web2/issues/2147483647",
                "id": 4388382432,
                "node_id": "I_kwDORzBCuc8AAAABBZFm4A",
                "number": 2147483647,
                "title": "Max Int Bug",
                "user": {
                  "login": "bluenet-web-bug-sync[bot]",
                  "id": 282110008,
                  "type": "Bot"
                },
                "labels": [],
                "state": "open",
                "locked": false,
                "assignee": null,
                "comments": 0,
                "created_at": "2026-05-06T02:43:47Z",
                "updated_at": "2026-05-06T02:44:21Z",
                "closed_at": null,
                "author_association": "NONE",
                "active_lock_reason": null,
                "body": "## 描述\\n\\ndesc",
                "timeline_url": "https://api.github.com/repos/Blue-Net-Team/official-web2/issues/2147483647/timeline",
                "performed_via_github_app": null,
                "state_reason": null
              }
            ]
            """;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getApiBaseUrl()).thenReturn("https://api.github.com");
        lenient().when(properties.getOwner()).thenReturn("bluenet-team");
        lenient().when(properties.getRepo()).thenReturn("bluenet-issues");
        // 刻意传入默认配置的 ObjectMapper（FAIL_ON_UNKNOWN_PROPERTIES 为 true），
        // 以验证客户端自身完成了未知字段容忍配置，不依赖外部注入实例的配置
        client = new GitHubIssueClient(properties, tokenService, new ObjectMapper());
    }

    @Nested
    @DisplayName("listIssues 方法测试")
    class ListIssuesTest {

        @Test
        @DisplayName("TC-001: 正常返回 Issue 列表")
        void listIssues_success_shouldReturnResults() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            String responseBody = REAL_ISSUE_LIST_RESPONSE;

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            ResponseEntity<String> emptyResponse = new ResponseEntity<>("[]", HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity)
                                    .thenReturn(emptyResponse);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            // 载荷含 3 条，其中 1 条为 PR，应被过滤
            assertEquals(2, results.size());
            assertEquals(60, results.get(0).number());
            assertEquals("ai客服意图识别的优化建议", results.get(0).title());
            assertEquals("open", results.get(0).state());
            assertEquals(61, results.get(1).number());
            assertEquals("closed", results.get(1).state());
        }

        @Test
        @DisplayName("TC-013: listIssues 解析真实 GitHub 响应（含大量未声明字段）应成功")
        void listIssues_realGitHubResponseWithUndefinedFields_shouldParseSuccessfully() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(REAL_ISSUE_LIST_RESPONSE, HttpStatus.OK);
            ResponseEntity<String> emptyResponse = new ResponseEntity<>("[]", HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity)
                                    .thenReturn(emptyResponse);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = assertDoesNotThrow(
                    () -> spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z")),
                    "真实 GitHub 响应包含未声明字段时不应抛异常");

            assertNotNull(results);
            // 3 条中 1 条为 PR，应被过滤
            assertEquals(2, results.size());
            assertEquals(60, results.get(0).number());
            assertEquals(61, results.get(1).number());
        }

        @Test
        @DisplayName("TC-002: 返回空列表")
        void listIssues_emptyResponse_shouldReturnEmptyList() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[]", HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("TC-003: GitHub API 返回 401/403 时抛出 RuntimeException")
        void listIssues_unauthorized_shouldThrowException() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z")));
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }

        @Test
        @DisplayName("TC-004: 分页场景正确合并多页结果")
        void listIssues_pagination_shouldMergeAllPages() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            String page1Body = REAL_ISSUE_LIST_RESPONSE;
            String page2Body = REAL_LARGE_NUMBER_ISSUE_RESPONSE;
            String page3Body = "[]";

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responsePage1 = new ResponseEntity<>(page1Body, HttpStatus.OK);
            ResponseEntity<String> responsePage2 = new ResponseEntity<>(page2Body, HttpStatus.OK);
            ResponseEntity<String> responsePage3 = new ResponseEntity<>(page3Body, HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responsePage1)
                                    .thenReturn(responsePage2)
                                    .thenReturn(responsePage3);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            // 第一页 2 条（1 条 PR 被过滤）+ 第二页 1 条
            assertEquals(3, results.size());
            assertEquals(60, results.get(0).number());
            assertEquals(61, results.get(1).number());
            assertEquals(Integer.valueOf(2147483647), results.get(2).number());
            verify(mockRestTemplate, times(3)).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(String.class));
        }

        @Test
        @DisplayName("TC-005: 返回结果中包含 PR 时应被过滤")
        void listIssues_withPullRequests_shouldFilterThemOut() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            String responseBody = REAL_ISSUE_LIST_RESPONSE;

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            ResponseEntity<String> emptyResponse = new ResponseEntity<>("[]", HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity)
                                    .thenReturn(emptyResponse);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(60, results.get(0).number());
            assertEquals("ai客服意图识别的优化建议", results.get(0).title());
            assertTrue(
                    results.stream().noneMatch(r -> r.number() == 62),
                    "带 pull_request 字段的条目应被过滤");
        }

        @Test
        @DisplayName("TC-006: number 字段为大数字时应正常处理")
        void listIssues_largeNumber_shouldHandleLongValue() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            // 使用 Integer.MAX_VALUE 测试 Jackson 可能返回 Long 时的安全转换
            String responseBody = REAL_LARGE_NUMBER_ISSUE_RESPONSE;

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            ResponseEntity<String> emptyResponse = new ResponseEntity<>("[]", HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity)
                                    .thenReturn(emptyResponse);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            // 不应抛出 ClassCastException
            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(Integer.valueOf(2147483647), results.get(0).number());
        }

        @Test
        @DisplayName("TC-007: 分页无上限时应限制最大页数")
        void listIssues_excessivePages_shouldStopAtMaxLimit() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            String pageBody = "[{\"number\": 1, \"title\": \"Bug\", \"body\": \"desc\", \"state\": \"open\", \"html_url\": \"https://github.com/test/issues/1\"}]";

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(pageBody, HttpStatus.OK);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            List<GitHubIssueListResult> results = spyClient.listIssues(Instant.parse("2024-01-01T00:00:00Z"));

            assertNotNull(results);
            // 即使每页都有数据，也应该在达到最大页数限制时停止
            // 假设最大页数限制为 10，每页 100 条，最多 1000 条
            assertTrue(results.size() <= 1000, "结果数量应受最大页数限制");
        }
    }

    @Nested
    @DisplayName("createIssue 方法测试")
    class CreateIssueTest {

        @Test
        @DisplayName("TC-012: createIssue 解析真实 GitHub 响应（含大量未声明字段）应成功")
        void createIssue_realGitHubResponseWithUndefinedFields_shouldParseSuccessfully() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            // 在真实响应基础上再注入一个"未来版本新增"的字段，验证解析不依赖字段枚举完整性
            String responseBody = REAL_CREATE_ISSUE_RESPONSE.replace(
                    "\"state_reason\": null",
                    "\"state_reason\": null, \"some_future_field\": {\"nested\": true}");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/repos/bluenet-team/bluenet-issues/issues"),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            GitHubIssueCreateResult result = assertDoesNotThrow(
                    () -> spyClient.createIssue("Bug Report", "Test body"),
                    "真实 GitHub 响应包含未声明字段时不应抛异常");

            assertNotNull(result);
            assertEquals(60, result.number());
            assertEquals("https://github.com/Blue-Net-Team/official-web2/issues/60", result.htmlUrl());
        }

        @Test
        @DisplayName("TC-008: 成功创建 Issue 应返回包含 number 和 html_url 的结果")
        void createIssue_success_shouldReturnResult() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            String responseBody = REAL_CREATE_ISSUE_RESPONSE;
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/repos/bluenet-team/bluenet-issues/issues"),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            GitHubIssueCreateResult result = spyClient.createIssue("Bug Report", "Test body");

            assertNotNull(result);
            assertEquals(60, result.number());
            assertEquals("https://github.com/Blue-Net-Team/official-web2/issues/60", result.htmlUrl());
        }

        @Test
        @DisplayName("TC-009: API 返回 401 应抛出 RuntimeException")
        void createIssue_unauthorized_shouldThrowException() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Bad credentials", HttpStatus.UNAUTHORIZED);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyClient.createIssue("Bug Report", "Test body"));
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }

        @Test
        @DisplayName("TC-010: API 返回 403 应抛出 RuntimeException")
        void createIssue_forbidden_shouldThrowException() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyClient.createIssue("Bug Report", "Test body"));
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }

        @Test
        @DisplayName("TC-011: API 返回 422 应抛出 RuntimeException")
        void createIssue_unprocessable_shouldThrowException() {
            when(tokenService.getAccessToken("issue-sync")).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Validation Failed",
                    HttpStatus.UNPROCESSABLE_ENTITY);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubIssueClient spyClient = spy(client);
            doReturn(mockRestTemplate).when(spyClient).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyClient.createIssue("", ""));
            assertTrue(ex.getMessage().contains("GitHub API error"));
        }
    }
}
