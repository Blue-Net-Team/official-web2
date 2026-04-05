package com.bluenet.web.api.controller.v1;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
@DisplayName("GitHub OAuth 集成测试")
class AuthControllerGithubIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_STUDENT_ID = "2024001999";
    private static final String TEST_PASSWORD = "testPassword123";

    @BeforeEach
    void createTestUser() {
        User existing = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getStudentId, TEST_STUDENT_ID));
        if (existing != null) {
            userMapper.deleteById(existing.getId());
        }

        User user = new User();
        user.setStudentId(TEST_STUDENT_ID);
        user.setUsername("github_test_user");
        user.setEmail("github_test@example.com");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRoleId(1L);
        user.setDisable(false);
        user.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(user);
    }

    private List<String> loginAndGetCookies() {
        Map<String, String> loginRequest = Map.of(
                "studentId",
                TEST_STUDENT_ID,
                "password",
                TEST_PASSWORD);

        ResponseEntity<ResponseMessage<Map<String, Object>>> response = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<Map<String, Object>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies, "Login should set cookies");
        return cookies;
    }

    private HttpEntity<Void> createAuthenticatedEntity(List<String> cookies) {
        return createAuthenticatedEntity(cookies, null);
    }

    private HttpEntity<Void> createAuthenticatedEntity(List<String> cookies, String csrfToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, cookies);
        if (csrfToken != null) {
            headers.set("X-CSRF-Token", csrfToken);
        }
        return new HttpEntity<>(headers);
    }

    // ==================== 发起登录测试 ====================

    @Nested
    @DisplayName("发起 GitHub 登录")
    class InitiateGithubLoginTest {

        @Test
        @DisplayName("应返回 GitHub 授权 URL")
        void shouldReturnGithubAuthorizeUrl() {
            ResponseEntity<ResponseMessage<String>> response = restTemplate.exchange(
                    "/api/v1/auth/github",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            String authorizeUrl = response.getBody().getData();
            assertNotNull(authorizeUrl);
            assertTrue(authorizeUrl.contains("github.com/login/oauth/authorize"));
            assertTrue(authorizeUrl.contains("client_id="));
            assertTrue(authorizeUrl.contains("state="));
            log.info("GitHub authorize URL: {}", authorizeUrl);
        }
    }

    // ==================== 绑定状态测试 ====================

    @Nested
    @DisplayName("查询绑定状态")
    class BindingStatusTest {

        @Test
        @DisplayName("未登录查询绑定状态应返回 401 或要求认证")
        void status_unauthenticated_shouldFail() {
            ResponseEntity<ResponseMessage<String>> response = restTemplate.exchange(
                    "/api/v1/auth/github/status",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            // 未登录应该返回非200或data为null
            assertTrue(
                    response.getStatusCode().is4xxClientError()
                            || response.getStatusCode().is5xxServerError()
                            || (response.getBody() != null && response.getBody().getCode() != 200),
                    "Unauthenticated request should not succeed with 200");
        }

        @Test
        @DisplayName("已登录未绑定时应返回 null")
        void status_authenticatedUnbound_shouldReturnNull() {
            List<String> cookies = loginAndGetCookies();

            ResponseEntity<ResponseMessage<String>> response = restTemplate.exchange(
                    "/api/v1/auth/github/status",
                    HttpMethod.GET,
                    createAuthenticatedEntity(cookies),
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            // 未绑定应返回 null
            assertNull(response.getBody().getData());
        }
    }

    // ==================== 发起绑定测试 ====================

    @Nested
    @DisplayName("发起 GitHub 绑定")
    class InitiateGithubBindTest {

        @Test
        @DisplayName("已登录应返回绑定授权 URL")
        void bind_authenticated_shouldReturnBindUrl() {
            List<String> cookies = loginAndGetCookies();

            ResponseEntity<ResponseMessage<String>> response = restTemplate.exchange(
                    "/api/v1/auth/github/bind",
                    HttpMethod.GET,
                    createAuthenticatedEntity(cookies),
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());

            String bindUrl = response.getBody().getData();
            assertNotNull(bindUrl);
            assertTrue(bindUrl.contains("github.com/login/oauth/authorize"));
            assertTrue(bindUrl.contains("state="));
            log.info("GitHub bind URL: {}", bindUrl);
        }

        @Test
        @DisplayName("未登录发起绑定应失败")
        void bind_unauthenticated_shouldFail() {
            ResponseEntity<ResponseMessage<String>> response = restTemplate.exchange(
                    "/api/v1/auth/github/bind",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            assertTrue(
                    response.getStatusCode().is4xxClientError()
                            || response.getStatusCode().is5xxServerError()
                            || (response.getBody() != null && response.getBody().getCode() != 200),
                    "Unauthenticated bind request should not succeed");
        }
    }

    // ==================== 回调参数验证测试 ====================

    @Nested
    @DisplayName("GitHub 回调参数验证")
    class CallbackValidationTest {

        @Test
        @DisplayName("无效的 state 应返回非 500 响应")
        void callback_invalidState_shouldNotReturn500() {
            // The callback does sendRedirect which returns 302.
            // TestRestTemplate follows redirects, so it will try to connect to
            // the frontend (localhost:3000) which is not running in tests.
            // We just verify the endpoint doesn't throw an unhandled 500 error.
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        "/api/v1/auth/github/callback?code=test-code&state=invalid-state",
                        HttpMethod.GET,
                        null,
                        String.class);
                // If we get here, the redirect target might have responded
                // Either way, it should not be a 500 from our backend
                assertNotEquals(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        response.getStatusCode(),
                        "Callback should not return 500 for invalid state");
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Expected: redirect followed to localhost:3000 which is not running
                // This means our backend correctly issued the redirect
                log.info("Redirect to frontend failed (expected in test env): {}", e.getMessage());
                assertTrue(
                        e.getMessage().contains("Connection refused") || e.getMessage().contains("I/O error"),
                        "Should be a connection error to the frontend, not a backend error");
            }
        }
    }

    // ==================== 解绑测试 ====================

    @Nested
    @DisplayName("解绑 GitHub")
    class UnbindGithubTest {

        @Test
        @DisplayName("未登录解绑应失败")
        void unbind_unauthenticated_shouldFail() {
            HttpHeaders headers = new HttpHeaders();
            // Try to extract CSRF token from cookies first
            ResponseEntity<ResponseMessage<String>> statusResponse = restTemplate.exchange(
                    "/api/v1/auth/github/status",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ResponseMessage<String>>() {
                    });

            ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                    "/api/v1/auth/github/bind",
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ResponseMessage<Void>>() {
                    });

            assertTrue(
                    response.getStatusCode().is4xxClientError()
                            || response.getStatusCode().is5xxServerError()
                            || (response.getBody() != null && response.getBody().getCode() != 200),
                    "Unauthenticated unbind should not succeed");
        }
    }
}
