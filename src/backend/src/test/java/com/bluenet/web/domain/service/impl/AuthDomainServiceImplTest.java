package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.exception.Unauthorized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link AuthDomainServiceImpl} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthDomainServiceImpl 单元测试")
class AuthDomainServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    private AuthDomainService authDomainService;

    @BeforeEach
    void setUp() {
        authDomainService = new AuthDomainServiceImpl(userRepository, passwordEncoder, verificationCodeRepository);
    }

    @Test
    @DisplayName("学号登录: 密码正确时返回用户")
    void checkLocalValid_studentIdCorrectPassword_shouldReturnUser() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setStudentId("2024001001");
        user.setDisable(false);
        when(userRepository.findByStudentId("2024001001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        Optional<User> result = authDomainService.checkLocalValid("2024001001", "password", LocalLoginType.STUDENT_ID);

        assertTrue(result.isPresent());
        assertEquals("2024001001", result.get().getStudentId());
    }

    @Test
    @DisplayName("学号登录: 密码错误时返回空")
    void checkLocalValid_studentIdWrongPassword_shouldReturnEmpty() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setStudentId("2024001001");
        user.setDisable(false);
        when(userRepository.findByStudentId("2024001001")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        Optional<User> result = authDomainService.checkLocalValid("2024001001", "wrong", LocalLoginType.STUDENT_ID);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("学号登录: 用户不存在时抛 Unauthorized")
    void checkLocalValid_studentIdNotFound_shouldThrow() {
        when(userRepository.findByStudentId("2024001001")).thenReturn(Optional.empty());

        assertThrows(
                Unauthorized.class,
                () -> authDomainService.checkLocalValid("2024001001", "password", LocalLoginType.STUDENT_ID));
    }

    @Test
    @DisplayName("学号登录: 账户被禁用时应抛 Unauthorized")
    void checkLocalValid_disabledAccount_shouldThrow() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setStudentId("2024001001");
        user.setDisable(true);
        when(userRepository.findByStudentId("2024001001")).thenReturn(Optional.of(user));

        assertThrows(
                Unauthorized.class,
                () -> authDomainService.checkLocalValid("2024001001", "password", LocalLoginType.STUDENT_ID));
    }

    @Test
    @DisplayName("邮箱登录: 验证码有效时返回用户")
    void checkLocalValid_emailCorrectCode_shouldReturnUser() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setEmail("test@example.com");
        user.setDisable(false);
        VerifyCode code = VerifyCode.create("test@example.com", "123456", LocalDateTime.now().plusMinutes(5), "LOGIN");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456")).thenReturn(Optional.of(code));

        Optional<User> result = authDomainService.checkLocalValid("test@example.com", "123456", LocalLoginType.EMAIL);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    @DisplayName("邮箱登录: 验证码不存在时返回空")
    void checkLocalValid_emailCodeNotFound_shouldReturnEmpty() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setEmail("test@example.com");
        user.setDisable(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456")).thenReturn(Optional.empty());

        Optional<User> result = authDomainService.checkLocalValid("test@example.com", "123456", LocalLoginType.EMAIL);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("邮箱登录: 验证码已过期时返回空")
    void checkLocalValid_emailCodeExpired_shouldReturnEmpty() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setEmail("test@example.com");
        user.setDisable(false);
        VerifyCode code = VerifyCode.create("test@example.com", "123456", LocalDateTime.now().minusMinutes(1), "LOGIN");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456")).thenReturn(Optional.of(code));

        Optional<User> result = authDomainService.checkLocalValid("test@example.com", "123456", LocalLoginType.EMAIL);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("邮箱登录: 验证码已使用时返回空")
    void checkLocalValid_emailCodeUsed_shouldReturnEmpty() {
        User user = User.reconstruct(1L, "encodedPassword");
        user.setEmail("test@example.com");
        user.setDisable(false);
        VerifyCode code = VerifyCode.reconstruct(
                1L,
                "test@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now(),
                "LOGIN");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationCodeRepository.findByEmailAndCode("test@example.com", "123456")).thenReturn(Optional.of(code));

        Optional<User> result = authDomainService.checkLocalValid("test@example.com", "123456", LocalLoginType.EMAIL);

        assertFalse(result.isPresent());
    }
}
