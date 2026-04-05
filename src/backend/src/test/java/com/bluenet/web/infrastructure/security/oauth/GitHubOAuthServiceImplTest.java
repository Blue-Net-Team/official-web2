package com.bluenet.web.infrastructure.security.oauth;

import com.bluenet.web.domain.model.vo.GitHubUserInfo;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubOAuthServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceImplTest {

    @Mock
    private GitHubOAuthProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GitHubOAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getClientId()).thenReturn("test-client-id");
        lenient().when(properties.getClientSecret()).thenReturn("test-client-secret");
        service = new GitHubOAuthServiceImpl(properties, objectMapper);
    }

    @Nested
    @DisplayName("buildAuthorizeUrl 方法测试")
    class BuildAuthorizeUrlTest {

        @Test
        @DisplayName("应正确构建包含所有参数的授权 URL")
        void buildAuthorizeUrl_validParams_shouldReturnFullUrl() {
            String url = service.buildAuthorizeUrl("test-state-123", "http://localhost:8080/callback");

            assertTrue(url.contains("client_id=test-client-id"));
            assertTrue(url.contains("state=test-state-123"));
            assertTrue(url.contains("redirect_uri=http://localhost:8080/callback"));
            assertTrue(url.startsWith("https://github.com/login/oauth/authorize"));
        }

        @Test
        @DisplayName("不同 state 应生成不同 URL")
        void buildAuthorizeUrl_differentStates_shouldReturnDifferentUrls() {
            String url1 = service.buildAuthorizeUrl("state-aaa", "http://localhost:8080/callback");
            String url2 = service.buildAuthorizeUrl("state-bbb", "http://localhost:8080/callback");

            assertNotEquals(url1, url2);
            assertTrue(url1.contains("state-aaa"));
            assertTrue(url2.contains("state-bbb"));
        }
    }

    @Nested
    @DisplayName("exchangeCodeForToken 方法测试")
    class ExchangeCodeForTokenTest {

        @Test
        @DisplayName("成功换取 token 应返回 access_token")
        void exchangeCodeForToken_success_shouldReturnAccessToken() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            String responseBody = "{\"access_token\":\"ghu_test123\",\"token_type\":\"bearer\"}";
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            String token = spyService.exchangeCodeForToken("test-code", "http://localhost:8080/callback");

            assertEquals("ghu_test123", token);
        }

        @Test
        @DisplayName("GitHub 返回错误应抛出 RuntimeException")
        void exchangeCodeForToken_errorResponse_shouldThrowException() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            String responseBody = "{\"error\":\"bad_verification_code\"}";
            ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenReturn(responseEntity);

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> spyService.exchangeCodeForToken("bad-code", "http://localhost:8080/callback"));
            assertTrue(ex.getMessage().contains("GitHub OAuth token exchange failed"));
        }

        @Test
        @DisplayName("网络异常应抛出 RuntimeException")
        void exchangeCodeForToken_networkError_shouldThrowException() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.POST),
                            any(HttpEntity.class),
                            eq(String.class)))
                                    .thenThrow(new RuntimeException("Connection refused"));

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            assertThrows(
                    RuntimeException.class,
                    () -> spyService.exchangeCodeForToken("test-code", "http://localhost:8080/callback"));
        }
    }

    @Nested
    @DisplayName("getUserInfo 方法测试")
    class GetUserInfoTest {

        @Test
        @DisplayName("应正确解析 GitHub 用户信息（含 email）")
        void getUserInfo_withEmail_shouldReturnUserInfo() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);

            GitHubOAuthServiceImpl.GitHubApiResponse apiResponse = new GitHubOAuthServiceImpl.GitHubApiResponse();
            ReflectionTestUtils.setField(apiResponse, "id", 12345L);
            ReflectionTestUtils.setField(apiResponse, "login", "testuser");
            ReflectionTestUtils.setField(apiResponse, "name", "Test User");
            ReflectionTestUtils.setField(apiResponse, "email", "test@example.com");
            ReflectionTestUtils.setField(apiResponse, "avatarUrl", "https://avatar.url/test.png");

            ResponseEntity<GitHubOAuthServiceImpl.GitHubApiResponse> responseEntity = new ResponseEntity<>(apiResponse,
                    HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(GitHubOAuthServiceImpl.GitHubApiResponse.class)))
                                    .thenReturn(responseEntity);

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            GitHubUserInfo userInfo = spyService.getUserInfo("ghu_test123");

            assertEquals(12345L, userInfo.getId());
            assertEquals("testuser", userInfo.getLogin());
            assertEquals("Test User", userInfo.getName());
            assertEquals("test@example.com", userInfo.getEmail());
            assertEquals("https://avatar.url/test.png", userInfo.getAvatarUrl());
        }

        @Test
        @DisplayName("email 为 null 时应调用 fetchPrimaryEmail 获取")
        void getUserInfo_nullEmail_shouldFetchPrimaryEmail() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);

            GitHubOAuthServiceImpl.GitHubApiResponse apiResponse = new GitHubOAuthServiceImpl.GitHubApiResponse();
            ReflectionTestUtils.setField(apiResponse, "id", 12345L);
            ReflectionTestUtils.setField(apiResponse, "login", "testuser");
            ReflectionTestUtils.setField(apiResponse, "name", "Test User");
            ReflectionTestUtils.setField(apiResponse, "email", null);
            ReflectionTestUtils.setField(apiResponse, "avatarUrl", "https://avatar.url/test.png");

            ResponseEntity<GitHubOAuthServiceImpl.GitHubApiResponse> responseEntity = new ResponseEntity<>(apiResponse,
                    HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            eq("https://api.github.com/user"),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(GitHubOAuthServiceImpl.GitHubApiResponse.class)))
                                    .thenReturn(responseEntity);

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();
            doReturn("primary@example.com").when(spyService).fetchPrimaryEmail(any(RestTemplate.class), anyString());

            GitHubUserInfo userInfo = spyService.getUserInfo("ghu_test123");

            assertEquals("primary@example.com", userInfo.getEmail());
        }

        @Test
        @DisplayName("GitHub API 返回空 body 应抛出 RuntimeException")
        void getUserInfo_nullBody_shouldThrowException() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            ResponseEntity<GitHubOAuthServiceImpl.GitHubApiResponse> responseEntity = new ResponseEntity<>(null,
                    HttpStatus.OK);

            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(GitHubOAuthServiceImpl.GitHubApiResponse.class)))
                                    .thenReturn(responseEntity);

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            assertThrows(RuntimeException.class, () -> spyService.getUserInfo("ghu_test123"));
        }

        @Test
        @DisplayName("网络异常应抛出 RuntimeException")
        void getUserInfo_networkError_shouldThrowException() throws Exception {
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            when(
                    mockRestTemplate.exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(GitHubOAuthServiceImpl.GitHubApiResponse.class)))
                                    .thenThrow(new RuntimeException("Connection refused"));

            GitHubOAuthServiceImpl spyService = spy(service);
            doReturn(mockRestTemplate).when(spyService).createRestTemplate();

            assertThrows(RuntimeException.class, () -> spyService.getUserInfo("ghu_test123"));
        }
    }
}
