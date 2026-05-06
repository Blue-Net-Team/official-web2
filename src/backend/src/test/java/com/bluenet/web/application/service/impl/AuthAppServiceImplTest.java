package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AuthResult;
import com.bluenet.web.application.command.auth.AuthCommands;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.template.EmailVerificationCodeTemplate;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthAppServiceImpl单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthAppServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private CookieService cookieService;

    @Mock
    private CsrfTokenService csrfTokenService;

    @Mock
    private VerificationCodeDomainService verificationCodeDomainService;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private MessageDispatcher messageDispatcher;

    @Mock
    private EmailVerificationCodeTemplate emailVerificationCodeTemplate;

    @Mock
    private GitHubOAuthService gitHubOAuthService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GitHubOAuthProperties gitHubOAuthProperties;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthAppServiceImpl authAppService;

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

    private AuthCommands.StudentIdLoginCommand createLoginCommand() {
        return new AuthCommands.StudentIdLoginCommand(TEST_STUDENT_ID, TEST_PASSWORD);
    }

    // ==================== 学号登录测试 ====================

    @Test
    @DisplayName("学号登录：有效凭证应返回CSRF Token")
    void login_withValidCredentials_shouldReturnCsrfToken() {
        // 准备
        UserVO userVO = createTestUserVO();
        AuthCommands.StudentIdLoginCommand command = createLoginCommand();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.of(userVO));
        when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getJti(TEST_TOKEN)).thenReturn(TEST_JTI);
        when(csrfTokenService.generateCsrfToken()).thenReturn(TEST_CSRF_TOKEN);

        // 执行
        AuthResult.Login result = authAppService.login(command, response);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_CSRF_TOKEN, result.csrfToken());
        assertNotNull(result.user());
        assertEquals(TEST_USERNAME, result.user().getUsername());

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil).generateToken(TEST_USER_ID);
        verify(authTokenService).storeToken(TEST_JTI, TEST_USER_ID);
        verify(csrfTokenService).generateCsrfToken();
        verify(cookieService).setAuthCookies(response, TEST_TOKEN, TEST_CSRF_TOKEN);
    }

    @Test
    @DisplayName("学号登录：无效凭证应抛出 Unauthorized")
    void login_withInvalidCredentials_shouldThrowUnauthorized() {
        // 准备
        AuthCommands.StudentIdLoginCommand command = createLoginCommand();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(Unauthorized.class, () -> authAppService.login(command, response));

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil, never()).generateToken(any());
        verify(authTokenService, never()).storeToken(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), any(), any());
    }

    @Test
    @DisplayName("学号登录：被禁用账户应抛出 Unauthorized")
    void login_withDisabledAccount_shouldThrowUnauthorized() {
        // 准备
        AuthCommands.StudentIdLoginCommand command = createLoginCommand();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenThrow(new Unauthorized("账户已被禁用"));

        // 执行 & 验证
        Unauthorized exception = assertThrows(Unauthorized.class, () -> authAppService.login(command, response));
        assertEquals("账户已被禁用", exception.getMessage());
    }

    // ==================== 邮箱验证码登录测试 ====================

    @Test
    @DisplayName("邮箱登录：有效邮箱和验证码应返回 CSRF Token 和用户信息")
    void loginWithEmail_withValidCredentials_shouldReturnCsrfTokenAndUserInfo() {
        // 准备
        UserVO userVO = createTestUserVO();
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(TEST_EMAIL, TEST_VERIFY_CODE);

        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenReturn(Optional.of(userVO));
        when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getJti(TEST_TOKEN)).thenReturn(TEST_JTI);
        when(csrfTokenService.generateCsrfToken()).thenReturn(TEST_CSRF_TOKEN);

        // 执行
        AuthResult.Login result = authAppService.loginWithEmail(command, response);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_CSRF_TOKEN, result.csrfToken());
        assertNotNull(result.user());
        assertEquals(TEST_USERNAME, result.user().getUsername());

        verify(authDomainService).checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL);
        verify(verificationCodeRepository).markAsUsed(TEST_EMAIL, TEST_VERIFY_CODE);
        verify(jwtUtil).generateToken(TEST_USER_ID);
        verify(authTokenService).storeToken(TEST_JTI, TEST_USER_ID);
        verify(csrfTokenService).generateCsrfToken();
        verify(cookieService).setAuthCookies(response, TEST_TOKEN, TEST_CSRF_TOKEN);
    }

    @Test
    @DisplayName("邮箱登录：无效凭证应抛出 Unauthorized")
    void loginWithEmail_withInvalidCredentials_shouldThrowUnauthorized() {
        // 准备
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(TEST_EMAIL, TEST_VERIFY_CODE);

        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenReturn(Optional.empty());

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authAppService.loginWithEmail(command, response));
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
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(TEST_EMAIL, TEST_VERIFY_CODE);

        when(authDomainService.checkLocalValid(TEST_EMAIL, TEST_VERIFY_CODE, LocalLoginType.EMAIL))
                .thenThrow(new Unauthorized("账户已被禁用"));

        // 执行 & 验证
        Unauthorized exception = assertThrows(
                Unauthorized.class,
                () -> authAppService.loginWithEmail(command, response));
        assertEquals("账户已被禁用", exception.getMessage());
    }

    // ==================== 发送验证码测试 ====================

    @Test
    @DisplayName("发送验证码：正常发送应生成、存储并发送邮件")
    void sendVerificationCode_shouldGenerateStoreAndSendEmail() {
        // 准备
        AuthCommands.SendVerificationCodeCommand command = new AuthCommands.SendVerificationCodeCommand(TEST_EMAIL,
                "login");

        VerifyCodeVO verifyCodeVO = VerifyCodeVO.builder()
                .target(TEST_EMAIL)
                .code(TEST_VERIFY_CODE)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .scene("login")
                .build();
        when(verificationCodeDomainService.generateCode(TEST_EMAIL, "login"))
                .thenReturn(verifyCodeVO);

        // 执行
        authAppService.sendVerificationCode(command);

        // 验证
        verify(verificationCodeDomainService).generateCode(TEST_EMAIL, "login");
        verify(verificationCodeRepository).save(verifyCodeVO);
        ArgumentCaptor<com.bluenet.web.application.message.MessageRequest> messageCaptor = ArgumentCaptor
                .forClass(com.bluenet.web.application.message.MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(messageCaptor.capture());
        assertEquals(com.bluenet.web.domain.model.enumerate.MessageChannel.EMAIL, messageCaptor.getValue().channel());
        assertEquals(
                com.bluenet.web.domain.model.enumerate.MessageContentType.HTML,
                messageCaptor.getValue().contentType());
        assertEquals(TEST_EMAIL, messageCaptor.getValue().recipient());
        assertEquals("蓝网登录验证码", messageCaptor.getValue().subject());
    }
}
