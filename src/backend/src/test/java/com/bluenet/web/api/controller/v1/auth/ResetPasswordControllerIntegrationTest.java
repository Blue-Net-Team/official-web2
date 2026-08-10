package com.bluenet.web.api.controller.v1.auth;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.auth.ResetPasswordRequestDTO;
import com.bluenet.web.api.dto.auth.SendResetCodeRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyEmailRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyResetCodeRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyStudentRequestDTO;
import com.bluenet.web.application.result.resetpassword.ResetPasswordResult;
import com.bluenet.web.application.service.ResetPasswordAppService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ResetPasswordController 集成测试。
 * <p>
 * 验证密码重置分步流程与参数校验行为。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("ResetPasswordController 集成测试")
class ResetPasswordControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private ResetPasswordAppService resetPasswordAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("rate_limit:*")));
    }

    private static final String RESET_TOKEN = "reset-token";
    private static final String VERIFICATION_CODE = "123456";

    @Test
    @DisplayName("验证学号：存在的学号应返回 resetToken")
    void verifyStudent_withExistingStudentId_shouldReturnResetToken() throws Exception {
        when(resetPasswordAppService.verifyStudent(any()))
                .thenReturn(new ResetPasswordResult.VerifyStudent(RESET_TOKEN));

        VerifyStudentRequestDTO requestDTO = new VerifyStudentRequestDTO();
        requestDTO.setStudentId("2024006001");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(RESET_TOKEN));
    }

    @Test
    @DisplayName("验证学号：学号为空时应返回 400")
    void verifyStudent_withBlankStudentId_shouldReturn400() throws Exception {
        VerifyStudentRequestDTO requestDTO = new VerifyStudentRequestDTO();
        requestDTO.setStudentId("");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("学号不能为空"));
    }

    @Test
    @DisplayName("验证邮箱：匹配的邮箱应推进流程")
    void verifyEmail_withMatchingEmail_shouldReturnOk() throws Exception {
        when(resetPasswordAppService.verifyEmail(any()))
                .thenReturn(new ResetPasswordResult.VerifyEmail(RESET_TOKEN));

        VerifyEmailRequestDTO requestDTO = new VerifyEmailRequestDTO();
        requestDTO.setResetToken(RESET_TOKEN);
        requestDTO.setEmail("test@example.com");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(RESET_TOKEN));
    }

    @Test
    @DisplayName("验证邮箱：邮箱格式非法时应返回 400")
    void verifyEmail_withInvalidEmail_shouldReturn400() throws Exception {
        VerifyEmailRequestDTO requestDTO = new VerifyEmailRequestDTO();
        requestDTO.setResetToken("dummy-token");
        requestDTO.setEmail("not-an-email");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("发送验证码：流程到达邮箱验证后应返回 200")
    void sendCode_afterEmailVerification_shouldReturnOk() throws Exception {
        doNothing().when(resetPasswordAppService).sendCode(any());

        SendResetCodeRequestDTO requestDTO = new SendResetCodeRequestDTO();
        requestDTO.setResetToken(RESET_TOKEN);

        mockMvc.perform(
                post("/api/v1/auth/reset-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("验证验证码：正确验证码应推进流程")
    void verifyCode_withCorrectCode_shouldReturnOk() throws Exception {
        doNothing().when(resetPasswordAppService).verifyCode(any());

        VerifyResetCodeRequestDTO requestDTO = new VerifyResetCodeRequestDTO();
        requestDTO.setResetToken(RESET_TOKEN);
        requestDTO.setCode(VERIFICATION_CODE);

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("重置密码：完整流程后新密码提交应返回 200")
    void resetPassword_afterFullFlow_shouldReturnOk() throws Exception {
        when(resetPasswordAppService.verifyStudent(any()))
                .thenReturn(new ResetPasswordResult.VerifyStudent(RESET_TOKEN));
        when(resetPasswordAppService.verifyEmail(any()))
                .thenReturn(new ResetPasswordResult.VerifyEmail(RESET_TOKEN));
        doNothing().when(resetPasswordAppService).sendCode(any());
        doNothing().when(resetPasswordAppService).verifyCode(any());
        doNothing().when(resetPasswordAppService).resetPassword(any());

        VerifyStudentRequestDTO verifyStudentDTO = new VerifyStudentRequestDTO();
        verifyStudentDTO.setStudentId("2024006005");
        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyStudentDTO)))
                .andExpect(status().isOk());

        VerifyEmailRequestDTO verifyEmailDTO = new VerifyEmailRequestDTO();
        verifyEmailDTO.setResetToken(RESET_TOKEN);
        verifyEmailDTO.setEmail("test@example.com");
        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyEmailDTO)))
                .andExpect(status().isOk());

        SendResetCodeRequestDTO sendCodeDTO = new SendResetCodeRequestDTO();
        sendCodeDTO.setResetToken(RESET_TOKEN);
        mockMvc.perform(
                post("/api/v1/auth/reset-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendCodeDTO)))
                .andExpect(status().isOk());

        VerifyResetCodeRequestDTO verifyCodeDTO = new VerifyResetCodeRequestDTO();
        verifyCodeDTO.setResetToken(RESET_TOKEN);
        verifyCodeDTO.setCode(VERIFICATION_CODE);
        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDTO)))
                .andExpect(status().isOk());

        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setResetToken(RESET_TOKEN);
        requestDTO.setNewPassword("newEncodedPassword");
        requestDTO.setConfirmPassword("newEncodedPassword");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("重置密码：新密码与确认密码不一致时应返回 400")
    void resetPassword_withMismatchedPasswords_shouldReturn400() throws Exception {
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setResetToken("dummy-token");
        requestDTO.setNewPassword("newPassword");
        requestDTO.setConfirmPassword("differentPassword");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("新密码与确认密码不一致"));
    }

    @Test
    @DisplayName("重置密码：resetToken 为空时应返回 400")
    void resetPassword_withBlankResetToken_shouldReturn400() throws Exception {
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setResetToken("");
        requestDTO.setNewPassword("newPassword");
        requestDTO.setConfirmPassword("newPassword");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("resetToken不能为空"));
    }
}
