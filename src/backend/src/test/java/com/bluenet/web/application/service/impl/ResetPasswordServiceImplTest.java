package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.email.EmailSender;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("ResetPasswordServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceImplTest {

    @Mock
    private ResetPasswordStateService stateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeDomainService verificationCodeDomainService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private ResetPasswordServiceImpl resetPasswordService;

    private static final String TEST_STUDENT_ID = "2021001";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "test-uuid-token";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_CODE = "123456";

    private UserVO createTestUserVO() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .email(TEST_EMAIL)
                .build();
    }

    private VerifyCodeVO createTestVerifyCodeVO() {
        return VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(TEST_CODE)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .scene("reset_password")
                .build();
    }

    // ==================== verifyStudent ====================

    @Nested
    @DisplayName("verifyStudent 方法测试")
    class VerifyStudentTests {

        @Test
        @DisplayName("学号存在：应返回 resetToken")
        void verifyStudent_existingStudent_shouldReturnToken() {
            UserVO user = createTestUserVO();
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(user));
            when(stateService.create(TEST_STUDENT_ID, TEST_USER_ID)).thenReturn(TEST_TOKEN);

            String result = resetPasswordService.verifyStudent(TEST_STUDENT_ID);

            assertEquals(TEST_TOKEN, result);
            verify(stateService).create(TEST_STUDENT_ID, TEST_USER_ID);
        }

        @Test
        @DisplayName("学号不存在：应抛出 BadRequest")
        void verifyStudent_nonExistingStudent_shouldThrowBadRequest() {
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyStudent(TEST_STUDENT_ID));

            assertEquals("学号不存在", exception.getMessage());
            verify(stateService, never()).create(any(), any());
        }
    }

    // ==================== verifyEmail ====================

    @Nested
    @DisplayName("verifyEmail 方法测试")
    class VerifyEmailTests {

        @Test
        @DisplayName("邮箱匹配：应返回 resetToken")
        void verifyEmail_matchingEmail_shouldReturnToken() {
            UserVO user = createTestUserVO();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(1);
            when(stateService.getField(TEST_TOKEN, "studentId")).thenReturn(TEST_STUDENT_ID);
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(user));

            String result = resetPasswordService.verifyEmail(TEST_TOKEN, TEST_EMAIL);

            assertEquals(TEST_TOKEN, result);
            verify(stateService).update(eq(TEST_TOKEN), any(Map.class));
        }

        @Test
        @DisplayName("Token 不存在：应抛出 BadRequest")
        void verifyEmail_expiredToken_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(false);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyEmail(TEST_TOKEN, TEST_EMAIL));

            assertEquals("重置流程已过期，请重新开始", exception.getMessage());
        }

        @Test
        @DisplayName("步骤未达到：应抛出 BadRequest")
        void verifyEmail_wrongStep_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(0);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyEmail(TEST_TOKEN, TEST_EMAIL));

            assertEquals("请先完成上一步验证", exception.getMessage());
        }

        @Test
        @DisplayName("邮箱不匹配：应抛出 BadRequest")
        void verifyEmail_mismatchedEmail_shouldThrowBadRequest() {
            UserVO user = createTestUserVO();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(1);
            when(stateService.getField(TEST_TOKEN, "studentId")).thenReturn(TEST_STUDENT_ID);
            when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(user));

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyEmail(TEST_TOKEN, "wrong@example.com"));

            assertEquals("邮箱与学号不匹配", exception.getMessage());
            verify(stateService, never()).update(any(), any());
        }
    }

    // ==================== sendCode ====================

    @Nested
    @DisplayName("sendCode 方法测试")
    class SendCodeTests {

        @Test
        @DisplayName("正常发送验证码：应成功")
        void sendCode_normal_shouldSucceed() {
            VerifyCodeVO codeVO = createTestVerifyCodeVO();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(2);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findLatestByEmailAndSceneWithinSeconds(
                            TEST_EMAIL,
                            "reset_password",
                            60)).thenReturn(Optional.empty());
            when(verificationCodeDomainService.generateCode(TEST_EMAIL, "127.0.0.1", "reset_password"))
                    .thenReturn(codeVO);

            assertDoesNotThrow(() -> resetPasswordService.sendCode(TEST_TOKEN, "127.0.0.1"));

            verify(verificationCodeRepository).save(codeVO);
            verify(emailSender).sendHtmlAsync(eq(TEST_EMAIL), any(), any());
            verify(stateService).update(eq(TEST_TOKEN), any(Map.class));
        }

        @Test
        @DisplayName("发送频率过快：应抛出 BadRequest")
        void sendCode_tooFrequent_shouldThrowBadRequest() {
            VerifyCodeVO recentCode = createTestVerifyCodeVO();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(2);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findLatestByEmailAndSceneWithinSeconds(
                            TEST_EMAIL,
                            "reset_password",
                            60)).thenReturn(Optional.of(recentCode));

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.sendCode(TEST_TOKEN, "127.0.0.1"));

            assertEquals("发送过于频繁，请稍后再试", exception.getMessage());
            verify(verificationCodeDomainService, never()).generateCode(any(), any(), any());
        }

        @Test
        @DisplayName("邮箱状态丢失：应抛出 BadRequest")
        void sendCode_missingEmailState_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(2);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(null);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.sendCode(TEST_TOKEN, "127.0.0.1"));

            assertEquals("重置流程状态异常，请重新开始", exception.getMessage());
        }
    }

    // ==================== verifyCode ====================

    @Nested
    @DisplayName("verifyCode 方法测试")
    class VerifyCodeTests {

        @Test
        @DisplayName("验证码正确：应标记已使用并更新步骤")
        void verifyCode_correct_shouldSucceed() {
            VerifyCodeVO codeVO = createTestVerifyCodeVO();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(3);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findByEmailAndCodeAndScene(
                            TEST_EMAIL,
                            TEST_CODE,
                            "reset_password")).thenReturn(Optional.of(codeVO));

            assertDoesNotThrow(() -> resetPasswordService.verifyCode(TEST_TOKEN, TEST_CODE));

            verify(verificationCodeRepository).markAsUsed(TEST_EMAIL, TEST_CODE, "reset_password");
            verify(stateService).update(eq(TEST_TOKEN), any(Map.class));
        }

        @Test
        @DisplayName("验证码错误：应抛出 BadRequest")
        void verifyCode_wrongCode_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(3);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findByEmailAndCodeAndScene(
                            TEST_EMAIL,
                            "000000",
                            "reset_password")).thenReturn(Optional.empty());

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyCode(TEST_TOKEN, "000000"));

            assertEquals("验证码错误", exception.getMessage());
        }

        @Test
        @DisplayName("验证码已使用：应抛出 BadRequest")
        void verifyCode_usedCode_shouldThrowBadRequest() {
            VerifyCodeVO codeVO = VerifyCodeVO.builder()
                    .target(TEST_EMAIL)
                    .code(TEST_CODE)
                    .expireAt(LocalDateTime.now().plusMinutes(5))
                    .used(true)
                    .scene("reset_password")
                    .build();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(3);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findByEmailAndCodeAndScene(
                            TEST_EMAIL,
                            TEST_CODE,
                            "reset_password")).thenReturn(Optional.of(codeVO));

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyCode(TEST_TOKEN, TEST_CODE));

            assertEquals("验证码已使用", exception.getMessage());
        }

        @Test
        @DisplayName("验证码已过期：应抛出 BadRequest")
        void verifyCode_expiredCode_shouldThrowBadRequest() {
            VerifyCodeVO codeVO = VerifyCodeVO.builder()
                    .target(TEST_EMAIL)
                    .code(TEST_CODE)
                    .expireAt(LocalDateTime.now().minusMinutes(1))
                    .used(false)
                    .scene("reset_password")
                    .build();
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(3);
            when(stateService.getField(TEST_TOKEN, "email")).thenReturn(TEST_EMAIL);
            when(
                    verificationCodeRepository.findByEmailAndCodeAndScene(
                            TEST_EMAIL,
                            TEST_CODE,
                            "reset_password")).thenReturn(Optional.of(codeVO));

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyCode(TEST_TOKEN, TEST_CODE));

            assertEquals("验证码已过期", exception.getMessage());
        }

        @Test
        @DisplayName("Token 过期：应抛出 BadRequest")
        void verifyCode_expiredToken_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(false);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.verifyCode(TEST_TOKEN, TEST_CODE));

            assertEquals("重置流程已过期，请重新开始", exception.getMessage());
        }
    }

    // ==================== resetPassword ====================

    @Nested
    @DisplayName("resetPassword 方法测试")
    class ResetPasswordTests {

        @Test
        @DisplayName("正常重置密码：应成功更新密码并清理状态")
        void resetPassword_normal_shouldSucceed() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(4);
            when(stateService.getField(TEST_TOKEN, "userId")).thenReturn(TEST_USER_ID.toString());
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$encoded");

            assertDoesNotThrow(() -> resetPasswordService.resetPassword(TEST_TOKEN, "newPassword123"));

            verify(userRepository).updatePassword(TEST_USER_ID, "$2a$10$encoded");
            verify(authTokenService).revokeAllUserTokens(TEST_USER_ID);
            verify(stateService).delete(TEST_TOKEN);
        }

        @Test
        @DisplayName("Token 过期：应抛出 BadRequest")
        void resetPassword_expiredToken_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(false);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.resetPassword(TEST_TOKEN, "newPassword123"));

            assertEquals("重置流程已过期，请重新开始", exception.getMessage());
        }

        @Test
        @DisplayName("未完成验证码验证：应抛出 BadRequest")
        void resetPassword_wrongStep_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(3);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.resetPassword(TEST_TOKEN, "newPassword123"));

            assertEquals("请先完成上一步验证", exception.getMessage());
        }

        @Test
        @DisplayName("状态字段丢失：应抛出 BadRequest")
        void resetPassword_missingStateFields_shouldThrowBadRequest() {
            when(stateService.exists(TEST_TOKEN)).thenReturn(true);
            when(stateService.getStep(TEST_TOKEN)).thenReturn(4);
            when(stateService.getField(TEST_TOKEN, "userId")).thenReturn(null);

            BadRequest exception = assertThrows(
                    BadRequest.class,
                    () -> resetPasswordService.resetPassword(TEST_TOKEN, "newPassword123"));

            assertEquals("重置流程状态异常，请重新开始", exception.getMessage());
        }
    }
}
