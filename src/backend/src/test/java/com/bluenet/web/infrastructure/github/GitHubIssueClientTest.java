package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
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

    @BeforeEach
    void setUp() {
        lenient().when(properties.getApiBaseUrl()).thenReturn("https://api.github.com");
        lenient().when(properties.getOwner()).thenReturn("bluenet-team");
        lenient().when(properties.getRepo()).thenReturn("bluenet-issues");
        client = new GitHubIssueClient(properties, tokenService);
    }

    @Nested
    @DisplayName("listIssues 方法测试")
    class ListIssuesTest {

        @Test
        @DisplayName("TC-001: 正常返回 Issue 列表")
        void listIssues_success_shouldReturnResults() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

            String responseBody = "[{\"number\": 1, \"title\": \"Bug 1\", \"body\": \"desc 1\", \"state\": \"open\", \"html_url\": \"https://github.com/test/issues/1\"},"
                    + "{\"number\": 2, \"title\": \"Bug 2\", \"body\": \"desc 2\", \"state\": \"closed\", \"html_url\": \"https://github.com/test/issues/2\"}]";

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
            assertEquals(1, results.get(0).number());
            assertEquals("Bug 1", results.get(0).title());
            assertEquals("open", results.get(0).state());
            assertEquals(2, results.get(1).number());
            assertEquals("closed", results.get(1).state());
        }

        @Test
        @DisplayName("TC-002: 返回空列表")
        void listIssues_emptyResponse_shouldReturnEmptyList() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

            String page1Body = "[{\"number\": 1, \"title\": \"Bug 1\", \"body\": \"desc\", \"state\": \"open\", \"html_url\": \"https://github.com/test/issues/1\"}]";
            String page2Body = "[{\"number\": 2, \"title\": \"Bug 2\", \"body\": \"desc\", \"state\": \"closed\", \"html_url\": \"https://github.com/test/issues/2\"}]";
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
            assertEquals(2, results.size());
            assertEquals(1, results.get(0).number());
            assertEquals(2, results.get(1).number());
            verify(mockRestTemplate, times(3)).exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(String.class));
        }

        @Test
        @DisplayName("TC-005: 返回结果中包含 PR 时应被过滤")
        void listIssues_withPullRequests_shouldFilterThemOut() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

            String responseBody = "[{\"number\": 1, \"title\": \"Bug 1\", \"body\": \"desc\", \"state\": \"open\", \"html_url\": \"https://github.com/test/issues/1\"},"
                    + "{\"number\": 2, \"title\": \"PR 1\", \"body\": \"pr desc\", \"state\": \"open\", \"html_url\": \"https://github.com/test/pulls/2\", \"pull_request\": {\"url\": \"https://api.github.com/repos/test/pulls/2\"}}]";

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
            assertEquals(1, results.size());
            assertEquals(1, results.get(0).number());
            assertEquals("Bug 1", results.get(0).title());
        }

        @Test
        @DisplayName("TC-006: number 字段为大数字时应正常处理")
        void listIssues_largeNumber_shouldHandleLongValue() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

            // 使用 Integer.MAX_VALUE 测试 Jackson 可能返回 Long 时的安全转换
            String responseBody = "[{\"number\": 2147483647, \"title\": \"Max Int Bug\", \"body\": \"desc\", \"state\": \"open\", \"html_url\": \"https://github.com/test/issues/2147483647\"}]";

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
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
        @DisplayName("TC-008: 成功创建 Issue 应返回包含 number 和 html_url 的结果")
        void createIssue_success_shouldReturnResult() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            String responseBody = "{\"number\": 42, \"html_url\": \"https://github.com/bluenet-team/bluenet-issues/issues/42\", \"title\": \"Bug Report\"}";
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
            assertEquals(42, result.number());
            assertEquals("https://github.com/bluenet-team/bluenet-issues/issues/42", result.htmlUrl());
        }

        @Test
        @DisplayName("TC-009: API 返回 401 应抛出 RuntimeException")
        void createIssue_unauthorized_shouldThrowException() {
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
            when(tokenService.getInstallationAccessToken()).thenReturn("ghs_test_token");

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
