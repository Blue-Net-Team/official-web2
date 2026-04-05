package com.bluenet.web.api.controller.v1;

import com.bluenet.web.application.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController GitHub OAuth 端点测试")
@ExtendWith(MockitoExtension.class)
class AuthControllerGithubTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
        ReflectionTestUtils.setField(authController, "callbackBaseUrl", "http://localhost:8080");
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Nested
    @DisplayName("GET /api/v1/auth/github - 发起 GitHub 登录")
    class InitiateGithubLoginTest {

        @Test
        @DisplayName("应返回 GitHub 授权 URL")
        void shouldReturnGithubAuthorizeUrl() throws Exception {
            when(authService.initiateGithubLogin("http://localhost:8080"))
                    .thenReturn("https://github.com/login/oauth/authorize?client_id=test");

            mockMvc.perform(get("/api/v1/auth/github"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("https://github.com/login/oauth/authorize?client_id=test"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/github/callback - GitHub 回调")
    class GithubCallbackTest {

        @Test
        @DisplayName("回调应委托给 AuthService 处理")
        void callback_shouldDelegateToAuthService() throws Exception {
            doNothing().when(authService)
                    .handleGithubCallback(
                            eq("test-code"),
                            eq("test-state"),
                            eq("http://localhost:8080"),
                            any(HttpServletResponse.class));

            mockMvc.perform(
                    get("/api/v1/auth/github/callback")
                            .param("code", "test-code")
                            .param("state", "test-state"))
                    .andExpect(status().isOk());

            verify(authService).handleGithubCallback(
                    eq("test-code"),
                    eq("test-state"),
                    eq("http://localhost:8080"),
                    any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("缺少 code 参数应返回 400")
        void callback_missingCode_shouldReturn400() throws Exception {
            mockMvc.perform(
                    get("/api/v1/auth/github/callback")
                            .param("state", "test-state"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("缺少 state 参数应返回 400")
        void callback_missingState_shouldReturn400() throws Exception {
            mockMvc.perform(
                    get("/api/v1/auth/github/callback")
                            .param("code", "test-code"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/github/status - 查询绑定状态")
    class GithubBindingStatusTest {

        @Test
        @DisplayName("已绑定应返回 GitHub 用户名")
        void status_bound_shouldReturnUsername() throws Exception {
            when(authService.getGithubBindingStatus()).thenReturn("testuser");

            mockMvc.perform(get("/api/v1/auth/github/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("testuser"));
        }

        @Test
        @DisplayName("未绑定应返回 null")
        void status_unbound_shouldReturnNull() throws Exception {
            when(authService.getGithubBindingStatus()).thenReturn(null);

            mockMvc.perform(get("/api/v1/auth/github/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/github/bind - 发起 GitHub 绑定")
    class InitiateGithubBindTest {

        @Test
        @DisplayName("应返回 GitHub 授权 URL")
        void shouldReturnGithubBindUrl() throws Exception {
            when(authService.initiateGithubBind("http://localhost:8080"))
                    .thenReturn("https://github.com/login/oauth/authorize?client_id=test&state=bind123");

            mockMvc.perform(get("/api/v1/auth/github/bind"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data")
                                    .value("https://github.com/login/oauth/authorize?client_id=test&state=bind123"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/auth/github/bind - 解绑 GitHub")
    class UnbindGithubTest {

        @Test
        @DisplayName("解绑成功应返回 200")
        void unbind_success_shouldReturn200() throws Exception {
            doNothing().when(authService).unbindGithub();

            mockMvc.perform(delete("/api/v1/auth/github/bind"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(authService).unbindGithub();
        }
    }
}
