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
