package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.ResetPasswordResult;
import com.bluenet.web.application.command.resetpassword.ResetPasswordCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.service.ResetPasswordAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ResetPasswordAppServiceImpl 集成测试。
 * <p>
 * 验证密码重置流程中学号/邮箱查询、验证码校验和密码修改均通过 UserRepository 实体驱动接口完成。
 * </p>
 */
@DisplayName("ResetPasswordAppServiceImpl 集成测试")
class ResetPasswordAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ResetPasswordAppService resetPasswordAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetPasswordStateService resetPasswordStateService;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @MockBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockBean
    private MessageDispatcher messageDispatcher;

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
                java.time.LocalDateTime.now().plusMinutes(5),
                "reset_password");
        when(verificationCodeDomainService.generateCode(email, "reset_password")).thenReturn(code);
    }

    @Test
    @DisplayName("sendCode: 应生成验证码并推进流程")
    void sendCode_shouldGenerateCodeAndAdvance() {
        User user = createUser("2024004004");
        String token = resetPasswordStateService.create(user.getStudentId(), user.getId());
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
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
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
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
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
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
        resetPasswordStateService.update(token, java.util.Map.of("step", "2", "email", user.getEmail()));
        mockVerificationCode(user.getEmail());
        resetPasswordAppService.sendCode(new ResetPasswordCommands.SendCodeCommand(token));
        resetPasswordAppService.verifyCode(
                new ResetPasswordCommands.VerifyCodeCommand(token, "123456"));

        resetPasswordAppService.resetPassword(new ResetPasswordCommands.ResetPasswordCommand(token, "newPassword"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword", updated.getPassword()));
        assertFalse(resetPasswordStateService.exists(token));
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
