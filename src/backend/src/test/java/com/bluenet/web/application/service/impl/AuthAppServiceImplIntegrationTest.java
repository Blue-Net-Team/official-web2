package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.auth.AuthCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.result.auth.AuthResult;
import com.bluenet.web.application.service.AuthAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthAppServiceImpl 集成测试。
 * <p>
 * 按新测试策略：真实 Repository，DomainService 中无 @Transactional 的用 @MockitoBean， 外部基础设施
 * mock。本类验证应用服务层的编排、事务边界与响应格式。
 * </p>
 */
@DisplayName("AuthAppServiceImpl 集成测试")
class AuthAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthAppService authAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MessageDispatcher messageDispatcher;

    @MockitoBean
    private GitHubOAuthService gitHubOAuthService;

    @MockitoBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockitoBean
    private AuthDomainService authDomainService;

    @AfterEach
    void clearContext() {
        UserCTX.clear();
    }

    private User createMemberUser(String studentId) {
        return UserFixture.builder()
                .withStudentId(studentId)
                .withRoleId(RoleFixture.roleId(roleMapper, RoleType.MEMBER))
                .save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("login: 学号与编码密码登录应返回用户ID与CSRF令牌")
    void login_withStudentIdAndEncodedPassword_shouldReturnUserIdAndCsrfToken() {
        User user = createMemberUser("2024005001");
        when(authDomainService.checkLocalValid(user.getStudentId(), "password", LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.of(user));
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthCommands.StudentIdLoginCommand command = new AuthCommands.StudentIdLoginCommand(
                user.getStudentId(),
                "password");

        AuthResult.Login result = authAppService.login(command, response);

        assertEquals(user.getId(), result.userId());
        assertNotNull(result.csrfToken());
    }

    @Test
    @DisplayName("loginWithEmail: 有效验证码登录应返回用户ID与CSRF令牌")
    void loginWithEmail_withValidCode_shouldReturnUserIdAndCsrfToken() {
        User user = createMemberUser("2024005002");
        String code = "123456";
        when(authDomainService.checkLocalValid(user.getEmail(), code, LocalLoginType.EMAIL))
                .thenReturn(Optional.of(user));
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(user.getEmail(), code);

        AuthResult.Login result = authAppService.loginWithEmail(command, response);

        assertEquals(user.getId(), result.userId());
        assertNotNull(result.csrfToken());
    }

    @Test
    @DisplayName("loginWithEmail: 无效验证码应抛出未授权异常")
    void loginWithEmail_withInvalidCode_shouldThrowUnauthorized() {
        User user = createMemberUser("2024005003");
        when(authDomainService.checkLocalValid(user.getEmail(), "000000", LocalLoginType.EMAIL))
                .thenReturn(Optional.empty());
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(user.getEmail(), "000000");

        assertThrows(Unauthorized.class, () -> authAppService.loginWithEmail(command, response));
    }

    @Test
    @DisplayName("loginWithEmail: 过期验证码应抛出未授权异常")
    void loginWithEmail_withExpiredCode_shouldThrowUnauthorized() {
        User user = createMemberUser("2024005004");
        String code = "123456";
        when(authDomainService.checkLocalValid(user.getEmail(), code, LocalLoginType.EMAIL))
                .thenReturn(Optional.empty());
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthCommands.EmailLoginCommand command = new AuthCommands.EmailLoginCommand(user.getEmail(), code);

        assertThrows(Unauthorized.class, () -> authAppService.loginWithEmail(command, response));
    }

    @Test
    @DisplayName("sendVerificationCode: 应保存验证码并通过消息分发器发送邮件")
    void sendVerificationCode_shouldSaveVerifyCodeAndDispatchMessage() {
        String email = "code-test@example.com";
        String code = "123456";
        String scene = "login";
        VerifyCode verifyCode = VerifyCode.create(email, code, LocalDateTime.now().plusMinutes(5), scene);
        when(verificationCodeDomainService.generateCode(email, scene)).thenReturn(verifyCode);
        AuthCommands.SendVerificationCodeCommand command = new AuthCommands.SendVerificationCodeCommand(email, scene);

        authAppService.sendVerificationCode(command);

        Optional<VerifyCode> saved = verificationCodeRepository.findByEmailAndCode(email, code);
        assertTrue(saved.isPresent());
        assertEquals(scene, saved.get().getScene());

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();
        assertEquals(MessageChannel.EMAIL, request.channel());
        assertEquals(email, request.recipient());
    }

    @Test
    @DisplayName("getAuthMe: 未认证时应返回 authenticated=false")
    void getAuthMe_unauthenticated_shouldReturnNotAuthenticated() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResult.AuthMe result = authAppService.getAuthMe(response);

        assertFalse(result.authenticated());
        assertNull(result.csrfToken());
    }

    @Test
    @DisplayName("getAuthMe: 认证时应返回 authenticated=true 与 CSRF 令牌")
    void getAuthMe_authenticated_shouldReturnAuthenticatedWithCsrfToken() {
        User user = createMemberUser("2024005005");
        UserCTX.setPrincipal(new SecurityPrincipal(user, RoleType.MEMBER, Collections.emptySet()));
        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResult.AuthMe result = authAppService.getAuthMe(response);

        assertTrue(result.authenticated());
        assertNotNull(result.csrfToken());
    }

    @Test
    @DisplayName("logout: 应正常执行且不抛出异常")
    void logout_shouldNotThrow() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertDoesNotThrow(() -> authAppService.logout(response));
    }
}
