package com.bluenet.web.domain.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;

/**
 * AuthDomainServiceImpl单元测试
 */
@DisplayName("AuthDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AuthDomainServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private AuthDomainServiceImpl authDomainService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "encodedPassword123";
    private static final String TEST_RAW_PASSWORD = "rawPassword123";

    private UserVO createTestUserVO() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .email(TEST_EMAIL)
                .username("测试用户")
                .password(TEST_PASSWORD)
                .disabled(false)
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    /**
     * 验证本地登录：有效的学号和密码应返回用户信息
     */
    @Test
    @DisplayName("验证本地登录：有效的学号和密码应返回用户信息")
    void checkLocalValid_withValidStudentIdAndPassword_shouldReturnUserVO() {
        // 准备
        UserVO userVO = createTestUserVO();
        when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(userVO));
        when(passwordEncoder.matches(TEST_RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(
                TEST_STUDENT_ID,
                TEST_RAW_PASSWORD,
                LocalLoginType.STUDENT_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get().getId());
        assertEquals(TEST_STUDENT_ID, result.get().getStudentId());
        verify(userRepository).findByStudentId(TEST_STUDENT_ID);
        verify(passwordEncoder).matches(TEST_RAW_PASSWORD, TEST_PASSWORD);
    }

    /**
     * 验证本地登录：用户不存在应抛出 DataNotFound 异常
     */
    @Test
    @DisplayName("验证本地登录：用户不存在应抛出 DataNotFound 异常")
    void checkLocalValid_withNonExistentUser_shouldThrowDataNotFound() {
        // 准备
        when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(
                Unauthorized.class,
                () -> authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_RAW_PASSWORD, LocalLoginType.STUDENT_ID));
    }

    /**
     * 验证本地登录：账号被禁用应抛出 Unauthorized 异常
     */
    @Test
    @DisplayName("验证本地登录：账号被禁用应抛出 Unauthorized 异常")
    void checkLocalValid_withDisabledAccount_shouldThrowUnauthorized() {
        // 准备
        UserVO userVO = UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .password(TEST_PASSWORD)
                .disabled(true)
                .build();
        when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(userVO));

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_RAW_PASSWORD, LocalLoginType.STUDENT_ID));
        assertEquals("账户已被禁用", exception.getMessage());
    }

    /**
     * 验证本地登录：密码错误应返回空
     */
    @Test
    @DisplayName("验证本地登录：密码错误应返回空")
    void checkLocalValid_withIncorrectPassword_shouldReturnEmpty() {
        // 准备
        UserVO userVO = createTestUserVO();
        when(userRepository.findByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(userVO));
        when(passwordEncoder.matches(TEST_RAW_PASSWORD, TEST_PASSWORD)).thenReturn(false);

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(
                TEST_STUDENT_ID,
                TEST_RAW_PASSWORD,
                LocalLoginType.STUDENT_ID);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 验证本地登录：有效的邮箱和验证码应返回用户信息
     */
    @Test
    @DisplayName("验证本地登录：有效的邮箱和验证码应返回用户信息")
    void checkLocalValid_withValidEmailAndVerifyCode_shouldReturnUserVO() {
        // 准备
        UserVO userVO = createTestUserVO();
        String verifyCode = "123456";
        VerifyCodeVO verifyCodeVO = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(verifyCode)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userVO));
        when(verificationCodeRepository.findByEmailAndCode(TEST_EMAIL, verifyCode))
                .thenReturn(Optional.of(verifyCodeVO));

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(TEST_EMAIL, verifyCode, LocalLoginType.EMAIL);

        // 验证
        assertTrue(result.isPresent());
        verify(verificationCodeRepository).findByEmailAndCode(TEST_EMAIL, verifyCode);
    }

    /**
     * 验证本地登录：验证码过期应返回空
     */
    @Test
    @DisplayName("验证本地登录：验证码过期应返回空")
    void checkLocalValid_withExpiredVerifyCode_shouldReturnEmpty() {
        // 准备
        UserVO userVO = createTestUserVO();
        String verifyCode = "123456";
        VerifyCodeVO verifyCodeVO = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(verifyCode)
                .expireAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userVO));
        when(verificationCodeRepository.findByEmailAndCode(TEST_EMAIL, verifyCode))
                .thenReturn(Optional.of(verifyCodeVO));

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(TEST_EMAIL, verifyCode, LocalLoginType.EMAIL);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 验证本地登录：验证码已使用应返回空
     */
    @Test
    @DisplayName("验证本地登录：验证码已使用应返回空")
    void checkLocalValid_withUsedVerifyCode_shouldReturnEmpty() {
        // 准备
        UserVO userVO = createTestUserVO();
        String verifyCode = "123456";
        VerifyCodeVO verifyCodeVO = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(verifyCode)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(true)
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userVO));
        when(verificationCodeRepository.findByEmailAndCode(TEST_EMAIL, verifyCode))
                .thenReturn(Optional.of(verifyCodeVO));

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(TEST_EMAIL, verifyCode, LocalLoginType.EMAIL);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 验证本地登录：验证码无效/不存在应返回空
     */
    @Test
    @DisplayName("验证本地登录：验证码无效/不存在应返回空")
    void checkLocalValid_withInvalidVerifyCode_shouldReturnEmpty() {
        // 准备
        UserVO userVO = createTestUserVO();
        String verifyCode = "123456";

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userVO));
        when(verificationCodeRepository.findByEmailAndCode(TEST_EMAIL, verifyCode)).thenReturn(Optional.empty());

        // 执行
        Optional<UserVO> result = authDomainService.checkLocalValid(TEST_EMAIL, verifyCode, LocalLoginType.EMAIL);

        // 验证
        assertFalse(result.isPresent());
    }

    /**
     * 验证本地登录：邮箱对应的用户不存在应抛出 Unauthorized
     */
    @Test
    @DisplayName("验证本地登录：邮箱对应的用户不存在应抛出 Unauthorized")
    void checkLocalValid_withNonExistentEmail_shouldThrowUnauthorized() {
        // 准备
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authDomainService.checkLocalValid("nonexistent@example.com", "123456", LocalLoginType.EMAIL));
        assertEquals("账号或密码错误", exception.getMessage());
    }
}
