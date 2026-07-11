package com.bluenet.web.api.controller.v1.auth;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.auth.ResetPasswordRequestDTO;
import com.bluenet.web.api.dto.auth.SendResetCodeRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyEmailRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyResetCodeRequestDTO;
import com.bluenet.web.api.dto.auth.VerifyStudentRequestDTO;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetPasswordStateService resetPasswordStateService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockBean
    private MessageDispatcher messageDispatcher;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("rate_limit:*")));
    }

    private static final String RESET_PASSWORD_SCENE = "reset_password";
    private static final String VERIFICATION_CODE = "123456";

    private User createUser(String studentId) {
        RoleDO memberRole = roleMapper.selectByName(RoleType.MEMBER.getName());
        User user = User.create(
                studentId,
                studentId + "@example.com",
                memberRole.getId(),
                passwordEncoder.encode("oldPassword"),
                "用户" + studentId,
                "昵称" + studentId,
                null,
                null,
                null,
                null,
                Gender.UNKNOWN,
                null,
                null,
                null,
                null,
                null,
                "REF" + studentId.substring(studentId.length() - 5),
                null);
        userRepository.save(user);
        return user;
    }

    private void mockVerificationCode(String email) {
        VerifyCode code = VerifyCode.create(
                email,
                VERIFICATION_CODE,
                LocalDateTime.now().plusMinutes(5),
                RESET_PASSWORD_SCENE);
        when(verificationCodeDomainService.generateCode(email, RESET_PASSWORD_SCENE)).thenReturn(code);
    }

    private SendResetCodeRequestDTO sendCodeRequest(String resetToken) {
        SendResetCodeRequestDTO requestDTO = new SendResetCodeRequestDTO();
        requestDTO.setResetToken(resetToken);
        return requestDTO;
    }

    @Test
    @DisplayName("验证学号：存在的学号应返回 resetToken")
    void verifyStudent_withExistingStudentId_shouldReturnResetToken() throws Exception {
        User user = createUser("2024006001");
        VerifyStudentRequestDTO requestDTO = new VerifyStudentRequestDTO();
        requestDTO.setStudentId(user.getStudentId());

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty());
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
        User user = createUser("2024006002");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        VerifyEmailRequestDTO requestDTO = new VerifyEmailRequestDTO();
        requestDTO.setResetToken(token);
        requestDTO.setEmail(user.getEmail());

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(token));

        assertTrue(resetPasswordStateService.exists(token));
        assertEquals("2", resetPasswordStateService.getField(token, "step"));
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
        User user = createUser("2024006003");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        SendResetCodeRequestDTO requestDTO = new SendResetCodeRequestDTO();
        requestDTO.setResetToken(token);

        mockMvc.perform(
                post("/api/v1/auth/reset-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals("3", resetPasswordStateService.getField(token, "step"));
    }

    @Test
    @DisplayName("验证验证码：正确验证码应推进流程")
    void verifyCode_withCorrectCode_shouldReturnOk() throws Exception {
        User user = createUser("2024006004");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        mockMvc.perform(
                post("/api/v1/auth/reset-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendCodeRequest(token))))
                .andExpect(status().isOk());

        VerifyResetCodeRequestDTO requestDTO = new VerifyResetCodeRequestDTO();
        requestDTO.setResetToken(token);
        requestDTO.setCode(VERIFICATION_CODE);

        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals("4", resetPasswordStateService.getField(token, "step"));
    }

    @Test
    @DisplayName("重置密码：完整流程后新密码应生效")
    void resetPassword_afterFullFlow_shouldUpdatePassword() throws Exception {
        User user = createUser("2024006005");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        mockMvc.perform(
                post("/api/v1/auth/reset-password/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendCodeRequest(token))))
                .andExpect(status().isOk());

        VerifyResetCodeRequestDTO verifyCodeDTO = new VerifyResetCodeRequestDTO();
        verifyCodeDTO.setResetToken(token);
        verifyCodeDTO.setCode(VERIFICATION_CODE);
        mockMvc.perform(
                post("/api/v1/auth/reset-password/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeDTO)))
                .andExpect(status().isOk());

        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setResetToken(token);
        requestDTO.setNewPassword("newEncodedPassword");
        requestDTO.setConfirmPassword("newEncodedPassword");

        mockMvc.perform(
                post("/api/v1/auth/reset-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newEncodedPassword", updated.getPassword()));
        assertFalse(resetPasswordStateService.exists(token));
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
