package com.bluenet.web.api.controller.v1;

import com.bluenet.web.application.service.ResetPasswordService;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ResetPasswordController 接口测试")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class ResetPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResetPasswordService resetPasswordService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRateLimitKeys() {
        redisTemplate.delete(redisTemplate.keys("rate_limit:*"));
    }

    private static final String BASE_URL = "/api/v1/auth/reset-password";
    private static final String TEST_TOKEN = "test-uuid-token";
    private static final String TEST_STUDENT_ID = "2021001";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "123456";

    // ==================== verifyStudent ====================

    @Nested
    @DisplayName("验证学号接口")
    class VerifyStudentTests {

        @Test
        @DisplayName("正常请求：应返回 200 和 resetToken")
        void verifyStudent_validRequest_shouldReturnToken() throws Exception {
            when(resetPasswordService.verifyStudent(TEST_STUDENT_ID)).thenReturn(TEST_TOKEN);

            mockMvc.perform(
                    post(BASE_URL + "/verify-student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"studentId\":\"" + TEST_STUDENT_ID + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(TEST_TOKEN));

            verify(resetPasswordService).verifyStudent(TEST_STUDENT_ID);
        }

        @Test
        @DisplayName("学号为空：应返回 400")
        void verifyStudent_emptyStudentId_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/verify-student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"studentId\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("学号不存在：应返回 400")
        void verifyStudent_nonExistingStudent_shouldReturn400() throws Exception {
            when(resetPasswordService.verifyStudent("9999999"))
                    .thenThrow(new com.bluenet.web.domain.exception.BadRequest("学号不存在"));

            mockMvc.perform(
                    post(BASE_URL + "/verify-student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"studentId\":\"9999999\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("学号不存在"));
        }
    }

    // ==================== verifyEmail ====================

    @Nested
    @DisplayName("验证邮箱接口")
    class VerifyEmailTests {

        @Test
        @DisplayName("正常请求：应返回 200 和 resetToken")
        void verifyEmail_validRequest_shouldReturnToken() throws Exception {
            when(resetPasswordService.verifyEmail(TEST_TOKEN, TEST_EMAIL)).thenReturn(TEST_TOKEN);

            mockMvc.perform(
                    post(BASE_URL + "/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\",\"email\":\"" + TEST_EMAIL + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(TEST_TOKEN));

            verify(resetPasswordService).verifyEmail(TEST_TOKEN, TEST_EMAIL);
        }

        @Test
        @DisplayName("缺少 resetToken：应返回 400")
        void verifyEmail_missingToken_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + TEST_EMAIL + "\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("邮箱格式不正确：应返回 400")
        void verifyEmail_invalidEmail_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\",\"email\":\"not-an-email\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== sendCode ====================

    @Nested
    @DisplayName("发送验证码接口")
    class SendCodeTests {

        @Test
        @DisplayName("正常请求：应返回 200")
        void sendCode_validRequest_shouldReturn200() throws Exception {
            doNothing().when(resetPasswordService).sendCode(TEST_TOKEN);

            mockMvc.perform(
                    post(BASE_URL + "/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(resetPasswordService).sendCode(TEST_TOKEN);
        }

        @Test
        @DisplayName("缺少 resetToken：应返回 400")
        void sendCode_missingToken_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Token 过期：应返回 400")
        void sendCode_expiredToken_shouldReturn400() throws Exception {
            doThrow(new com.bluenet.web.domain.exception.BadRequest("重置流程已过期，请重新开始"))
                    .when(resetPasswordService)
                    .sendCode("expired-token");

            mockMvc.perform(
                    post(BASE_URL + "/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"expired-token\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("重置流程已过期，请重新开始"));
        }
    }

    // ==================== verifyCode ====================

    @Nested
    @DisplayName("验证验证码接口")
    class VerifyCodeTests {

        @Test
        @DisplayName("验证码正确：应返回 200")
        void verifyCode_validRequest_shouldReturn200() throws Exception {
            doNothing().when(resetPasswordService).verifyCode(TEST_TOKEN, TEST_CODE);

            mockMvc.perform(
                    post(BASE_URL + "/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\",\"code\":\"" + TEST_CODE + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(resetPasswordService).verifyCode(TEST_TOKEN, TEST_CODE);
        }

        @Test
        @DisplayName("缺少验证码：应返回 400")
        void verifyCode_missingCode_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("验证码错误：应返回 400")
        void verifyCode_wrongCode_shouldReturn400() throws Exception {
            doThrow(new com.bluenet.web.domain.exception.BadRequest("验证码错误"))
                    .when(resetPasswordService)
                    .verifyCode(eq(TEST_TOKEN), eq("000000"));

            mockMvc.perform(
                    post(BASE_URL + "/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\",\"code\":\"000000\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("验证码错误"));
        }

        @Test
        @DisplayName("验证码已过期：应返回 400")
        void verifyCode_expiredCode_shouldReturn400() throws Exception {
            doThrow(new com.bluenet.web.domain.exception.BadRequest("验证码已过期"))
                    .when(resetPasswordService)
                    .verifyCode(TEST_TOKEN, TEST_CODE);

            mockMvc.perform(
                    post(BASE_URL + "/verify-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\",\"code\":\"" + TEST_CODE + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("验证码已过期"));
        }
    }

    // ==================== resetPassword ====================

    @Nested
    @DisplayName("重置密码接口")
    class ResetPasswordTests {

        @Test
        @DisplayName("正常请求：应返回 200")
        void resetPassword_validRequest_shouldReturn200() throws Exception {
            doNothing().when(resetPasswordService).resetPassword(TEST_TOKEN, "newPassword123");

            mockMvc.perform(
                    post(BASE_URL + "/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    "{\"resetToken\":\"" + TEST_TOKEN + "\","
                                            + "\"newPassword\":\"newPassword123\","
                                            + "\"confirmPassword\":\"newPassword123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(resetPasswordService).resetPassword(TEST_TOKEN, "newPassword123");
        }

        @Test
        @DisplayName("密码不一致：应返回 400")
        void resetPassword_mismatchedPasswords_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    "{\"resetToken\":\"" + TEST_TOKEN + "\","
                                            + "\"newPassword\":\"newPassword123\","
                                            + "\"confirmPassword\":\"differentPassword\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("新密码与确认密码不一致"));
        }

        @Test
        @DisplayName("缺少必填字段：应返回 400")
        void resetPassword_missingFields_shouldReturn400() throws Exception {
            mockMvc.perform(
                    post(BASE_URL + "/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"resetToken\":\"" + TEST_TOKEN + "\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("流程已过期：应返回 400")
        void resetPassword_expiredToken_shouldReturn400() throws Exception {
            doThrow(new com.bluenet.web.domain.exception.BadRequest("重置流程已过期，请重新开始"))
                    .when(resetPasswordService)
                    .resetPassword(eq("expired-token"), any());

            mockMvc.perform(
                    post(BASE_URL + "/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    "{\"resetToken\":\"expired-token\","
                                            + "\"newPassword\":\"newPassword123\","
                                            + "\"confirmPassword\":\"newPassword123\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.msg").value("重置流程已过期，请重新开始"));
        }
    }
}
