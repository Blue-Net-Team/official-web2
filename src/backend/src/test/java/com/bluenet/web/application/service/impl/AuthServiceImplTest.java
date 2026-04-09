package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.email.EmailSender;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserConverter userConverter;

    @Mock
    private CookieService cookieService;

    @Mock
    private CsrfTokenService csrfTokenService;

    @Mock
    private VerificationCodeDomainService verificationCodeDomainService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_JTI = "test-jti-123";
    private static final String TEST_CSRF_TOKEN = "test-csrf-token-456";
    private static final String TEST_VERIFY_CODE = "123456";

    private UserVO createTestUserVO() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .disabled(false)
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    private StudentIdLoginRequestDTO createLoginRequest() {
        StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
        request.setStudentId(TEST_STUDENT_ID);
        request.setPassword(TEST_PASSWORD);
        return request;
    }

    // ==================== 学号登录测试 ====================

    @Test
    @DisplayName("学号登录：有效凭证应返回CSRF Token")
    void login_withValidCredentials_shouldReturnCsrfToken() {
        // 准备
        UserVO userVO = createTestUserVO();
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.of(userVO));
        when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getJti(TEST_TOKEN)).thenReturn(TEST_JTI);
        when(csrfTokenService.generateCsrfToken()).thenReturn(TEST_CSRF_TOKEN);
        UserInfo expectedUserInfo = UserInfo.builder().username(TEST_USERNAME).build();
        when(userConverter.convertToUserInfo(userVO)).thenReturn(expectedUserInfo);

        // 执行
        UserAuthResponseDTO result = authService.login(request, response);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_CSRF_TOKEN, result.getCsrfToken());
        assertNotNull(result.getUserInfo());
        assertEquals(TEST_USERNAME, result.getUserInfo().getUsername());

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil).generateToken(TEST_USER_ID);
        verify(authTokenService).storeToken(TEST_JTI, TEST_USER_ID);
        verify(csrfTokenService).generateCsrfToken();
        verify(cookieService).setAuthCookies(response, TEST_TOKEN, TEST_CSRF_TOKEN);
        verify(userConverter).convertToUserInfo(userVO);
    }

    @Test
    @DisplayName("学号登录：无效凭证应抛出 Unauthorized")
    void login_withInvalidCredentials_shouldThrowUnauthorized() {
        // 准备
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(Unauthorized.class, () -> authService.login(request, response));

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil, never()).generateToken(any());
        verify(authTokenService, never()).storeToken(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), any(), any());
    }

    @Test
    @DisplayName("学号登录：被禁用账户应抛出 Unauthorized")
    void login_withDisabledAccount_shouldThrowUnauthorized() {
        // 准备
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenThrow(new Unauthorized("账户已被禁用"));

        // 执行 & 验证
        Unauthorized exception = assertThrows(Unauthorized.class, () -> authService.login(request, response));
        assertEquals("账户已被禁用", exception.getMessage());
    }

    // ==================== 邮箱验证码登录测试 ====================

    @Test
    @DisplayName("邮箱登录：有效邮箱和验证码应返回 CSRF Token 和用户信息")
    void loginWithEmail_withValidCredentials_shouldReturnCsrfTokenAndUserInfo() {
        // 准备
        UserVO userVO = createTestUserVO();

        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenReturn(Optional.of(userVO));
        when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getJti(TEST_TOKEN)).thenReturn(TEST_JTI);
        when(csrfTokenService.generateCsrfToken()).thenReturn(TEST_CSRF_TOKEN);
        UserInfo expectedUserInfo = UserInfo.builder().username(TEST_USERNAME).build();
        when(userConverter.convertToUserInfo(userVO)).thenReturn(expectedUserInfo);

        // 执行
        UserAuthResponseDTO result = authService.loginWithEmail(TEST_EMAIL, TEST_VERIFY_CODE, response);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_CSRF_TOKEN, result.getCsrfToken());
        assertNotNull(result.getUserInfo());
        assertEquals(TEST_USERNAME, result.getUserInfo().getUsername());

        verify(authDomainService).checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL);
        verify(verificationCodeRepository).markAsUsed(TEST_EMAIL, TEST_VERIFY_CODE);
        verify(jwtUtil).generateToken(TEST_USER_ID);
        verify(authTokenService).storeToken(TEST_JTI, TEST_USER_ID);
        verify(csrfTokenService).generateCsrfToken();
        verify(cookieService).setAuthCookies(response, TEST_TOKEN, TEST_CSRF_TOKEN);
        verify(userConverter).convertToUserInfo(userVO);
    }

    @Test
    @DisplayName("邮箱登录：无效凭证应抛出 Unauthorized")
    void loginWithEmail_withInvalidCredentials_shouldThrowUnauthorized() {
        // 准备
        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenReturn(Optional.empty());

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authService.loginWithEmail(TEST_EMAIL, TEST_VERIFY_CODE, response));
        assertEquals("邮箱或验证码错误", exception.getMessage());

        verify(jwtUtil, never()).generateToken(any());
        verify(authTokenService, never()).storeToken(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), any(), any());
        verify(verificationCodeRepository, never()).markAsUsed(any(), any());
    }

    @Test
    @DisplayName("邮箱登录：被禁用账户应抛出 Unauthorized")
    void loginWithEmail_withDisabledAccount_shouldThrowUnauthorized() {
        // 准备
        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenThrow(new Unauthorized("账户已被禁用"));

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authService.loginWithEmail(TEST_EMAIL, TEST_VERIFY_CODE, response));
        assertEquals("账户已被禁用", exception.getMessage());
    }

    // ==================== 发送验证码测试 ====================

    @Test
    @DisplayName("发送验证码：正常发送应生成、存储并发送邮件")
    void sendVerificationCode_withNoRecentCode_shouldSendEmail() {
        // 准备
        SendVerificationCodeRequestDTO request = new SendVerificationCodeRequestDTO();
        request.setEmail(TEST_EMAIL);

        when(verificationCodeRepository.findLatestByEmailWithinSeconds(TEST_EMAIL, 60))
                .thenReturn(Optional.empty());

        VerifyCodeVO verifyCodeVO = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(TEST_VERIFY_CODE)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(verificationCodeDomainService.generateCode(TEST_EMAIL, null, "login"))
                .thenReturn(verifyCodeVO);

        // 执行
        authService.sendVerificationCode(request);

        // 验证
        verify(verificationCodeRepository).findLatestByEmailWithinSeconds(TEST_EMAIL, 60);
        verify(verificationCodeDomainService).generateCode(TEST_EMAIL, null, "login");
        verify(verificationCodeRepository).save(verifyCodeVO);
        verify(emailSender).sendHtmlAsync(eq(TEST_EMAIL), eq("蓝网登录验证码"), anyString());
    }

    @Test
    @DisplayName("发送验证码：60秒内重复发送应抛出 BadRequest")
    void sendVerificationCode_withRecentCode_shouldThrowBadRequest() {
        // 准备
        SendVerificationCodeRequestDTO request = new SendVerificationCodeRequestDTO();
        request.setEmail(TEST_EMAIL);

        VerifyCodeVO recentCode = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code("654321")
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(verificationCodeRepository.findLatestByEmailWithinSeconds(TEST_EMAIL, 60))
                .thenReturn(Optional.of(recentCode));

        // 执行 & 验证
        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> authService.sendVerificationCode(request));
        assertEquals("发送过于频繁，请稍后再试", exception.getMessage());

        verify(verificationCodeDomainService, never()).generateCode(any(), any());
        verify(verificationCodeRepository, never()).save(any());
        verify(emailSender, never()).sendHtmlAsync(any(), any(), any());
    }
}
