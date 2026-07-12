package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.resetpassword.ResetPasswordCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.result.resetpassword.ResetPasswordResult;
import com.bluenet.web.application.service.ResetPasswordAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ResetPasswordAppServiceImpl 集成测试。
 *
 * <p>
 * 按新测试策略：真实 Repository，DomainService 中无 @Transactional 的用 @MockitoBean， 外部基础设施
 * mock。本类验证应用服务层的编排、事务边界与响应格式。
 * </p>
 */
@DisplayName("ResetPasswordAppServiceImpl 集成测试")
class ResetPasswordAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ResetPasswordAppService resetPasswordAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetPasswordStateService resetPasswordStateService;

    @MockitoBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockitoBean
    private MessageDispatcher messageDispatcher;

    @MockitoBean
    private AuthTokenService authTokenService;

    private User createUser(String studentId) {
        return UserFixture.member(studentId)
                .withPassword("oldPassword")
                .save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("verifyStudent: 应创建重置流程并返回 token")
    void verifyStudent_shouldCreateResetFlow() {
        User user = createUser("2024004001");

        ResetPasswordResult.VerifyStudent result = resetPasswordAppService.verifyStudent(
                new ResetPasswordCommands.VerifyStudentCommand(user.getStudentId()));

        assertNotNull(result.resetToken());
        assertTrue(resetPasswordStateService.exists(result.resetToken()));
        assertEquals("1", resetPasswordStateService.getField(result.resetToken(), "step"));
        assertEquals(user.getId().toString(), resetPasswordStateService.getField(result.resetToken(), "userId"));
    }

    @Test
    @DisplayName("verifyStudent: 学号不存在应抛异常")
    void verifyStudent_studentNotFound_shouldThrow() {
        assertThrows(
                BadRequest.class,
                () -> resetPasswordAppService
                        .verifyStudent(new ResetPasswordCommands.VerifyStudentCommand("9999999999")));
    }

    @Test
    @DisplayName("verifyEmail: 应验证邮箱并推进流程")
    void verifyEmail_shouldVerifyEmailAndAdvance() {
        User user = createUser("2024004002");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());

        ResetPasswordResult.VerifyEmail result = resetPasswordAppService.verifyEmail(
                new ResetPasswordCommands.VerifyEmailCommand(token, user.getEmail()));

        assertEquals(token, result.resetToken());
        assertEquals("2", resetPasswordStateService.getField(token, "step"));
        assertEquals(user.getEmail(), resetPasswordStateService.getField(token, "email"));
    }

    @Test
    @DisplayName("verifyEmail: 邮箱不匹配应抛异常")
    void verifyEmail_emailMismatch_shouldThrow() {
        User user = createUser("2024004003");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());

        assertThrows(
                BadRequest.class,
                () -> resetPasswordAppService.verifyEmail(
                        new ResetPasswordCommands.VerifyEmailCommand(token, "wrong@example.com")));
    }

    private void mockVerificationCode(String email) {
        VerifyCode code = VerifyCode.create(
                email,
                "123456",
                LocalDateTime.now().plusMinutes(5),
                "reset_password");
        when(verificationCodeDomainService.generateCode(email, "reset_password")).thenReturn(code);
    }

    @Test
    @DisplayName("sendCode: 应生成验证码并推进流程")
    void sendCode_shouldGenerateCodeAndAdvance() {
        User user = createUser("2024004004");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        resetPasswordAppService.sendCode(new ResetPasswordCommands.SendCodeCommand(token));

        assertEquals("3", resetPasswordStateService.getField(token, "step"));
        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        assertEquals(user.getEmail(), captor.getValue().recipient());
    }

    @Test
    @DisplayName("verifyCode: 应校验验证码并推进流程")
    void verifyCode_shouldVerifyCodeAndAdvance() {
        User user = createUser("2024004005");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        resetPasswordAppService.sendCode(new ResetPasswordCommands.SendCodeCommand(token));

        resetPasswordAppService.verifyCode(
                new ResetPasswordCommands.VerifyCodeCommand(token, "123456"));

        assertEquals("4", resetPasswordStateService.getField(token, "step"));
    }

    @Test
    @DisplayName("verifyCode: 错误验证码应抛异常")
    void verifyCode_wrongCode_shouldThrow() {
        User user = createUser("2024004006");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());

        resetPasswordAppService.sendCode(new ResetPasswordCommands.SendCodeCommand(token));

        assertThrows(
                BadRequest.class,
                () -> resetPasswordAppService.verifyCode(
                        new ResetPasswordCommands.VerifyCodeCommand(token, "000000")));
    }

    @Test
    @DisplayName("resetPassword: 应重置密码并清理流程")
    void resetPassword_shouldResetPasswordAndCleanUp() {
        User user = createUser("2024004007");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());
        resetPasswordAppService.sendCode(new ResetPasswordCommands.SendCodeCommand(token));
        resetPasswordAppService.verifyCode(
                new ResetPasswordCommands.VerifyCodeCommand(token, "123456"));

        resetPasswordAppService.resetPassword(new ResetPasswordCommands.ResetPasswordCommand(token, "newPassword"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword", updated.getPassword()));
        assertFalse(resetPasswordStateService.exists(token));
        verify(authTokenService).revokeAllUserTokens(user.getId());
    }

    @Test
    @DisplayName("resetPassword: 流程未到达最后一步应抛异常")
    void resetPassword_flowNotCompleted_shouldThrow() {
        User user = createUser("2024004008");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());

        assertThrows(
                BadRequest.class,
                () -> resetPasswordAppService.resetPassword(
                        new ResetPasswordCommands.ResetPasswordCommand(token, "newPassword")));
    }
}
