package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AuthResult;
import com.bluenet.web.application.command.auth.AuthCommands;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.template.EmailVerificationCodeTemplate;
import com.bluenet.web.application.message.template.VerificationCodeScene;
import com.bluenet.web.application.service.AuthAppService;
import com.bluenet.web.application.service.auth.credential.GitHubCallbackCredential;
import com.bluenet.web.application.service.auth.provider.EmailCodeLoginProvider;
import com.bluenet.web.application.service.auth.provider.GitHubAuthProvider;
import com.bluenet.web.application.service.auth.provider.StudentIdLoginProvider;
import com.bluenet.web.application.service.auth.session.AuthSessionIssuer;
import com.bluenet.web.application.service.auth.strategy.AuthProviderRegistry;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtPayload;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
import com.bluenet.web.infrastructure.security.oauth.OAuthStateStore;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务实现。
 * <p>
 * 实现认证聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@Service
public class AuthAppServiceImpl implements AuthAppService {

    private final AuthTokenService authTokenService;
    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageDispatcher messageDispatcher;
    private final EmailVerificationCodeTemplate emailVerificationCodeTemplate;
    private final AuthSessionIssuer authSessionIssuer;
    private final AuthProviderRegistry authProviderRegistry;

    /**
     * 保持原依赖注入入口，内部组合更细粒度 provider，避免应用服务继续膨胀。
     *
     * @param authDomainService
     *            认证领域服务
     * @param jwtUtil
     *            JWT工具
     * @param authTokenService
     *            认证令牌服务
     * @param cookieService
     *            Cookie服务
     * @param csrfTokenService
     *            CSRF令牌服务
     * @param verificationCodeDomainService
     *            验证码领域服务
     * @param verificationCodeRepository
     *            验证码仓储
     * @param messageDispatcher
     *            消息分发器
     * @param gitHubOAuthService
     *            GitHub OAuth服务
     * @param userRepository
     *            用户仓储
     * @param redisTemplate
     *            Redis模板
     * @param objectMapper
     *            对象映射器
     * @param gitHubOAuthProperties
     *            GitHub OAuth配置
     */
    public AuthAppServiceImpl(
            AuthDomainService authDomainService,
            JwtUtil jwtUtil,
            AuthTokenService authTokenService,
            CookieService cookieService,
            CsrfTokenService csrfTokenService,
            VerificationCodeDomainService verificationCodeDomainService,
            VerificationCodeRepository verificationCodeRepository,
            MessageDispatcher messageDispatcher,
            EmailVerificationCodeTemplate emailVerificationCodeTemplate,
            GitHubOAuthService gitHubOAuthService,
            UserRepository userRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            GitHubOAuthProperties gitHubOAuthProperties) {
        this.authTokenService = authTokenService;
        this.cookieService = cookieService;
        this.csrfTokenService = csrfTokenService;
        this.verificationCodeDomainService = verificationCodeDomainService;
        this.verificationCodeRepository = verificationCodeRepository;
        this.messageDispatcher = messageDispatcher;
        this.emailVerificationCodeTemplate = emailVerificationCodeTemplate;
        this.authSessionIssuer = new AuthSessionIssuer(
                jwtUtil,
                authTokenService,
                cookieService,
                csrfTokenService);
        StudentIdLoginProvider studentIdLoginProvider = new StudentIdLoginProvider(authDomainService);
        EmailCodeLoginProvider emailCodeLoginProvider = new EmailCodeLoginProvider(authDomainService,
                verificationCodeRepository);
        GitHubAuthProvider githubAuthProvider = new GitHubAuthProvider(
                gitHubOAuthService,
                userRepository,
                gitHubOAuthProperties,
                new OAuthStateStore(redisTemplate, objectMapper),
                authSessionIssuer);
        this.authProviderRegistry = new AuthProviderRegistry(
                List.of(studentIdLoginProvider, emailCodeLoginProvider, githubAuthProvider));
    }

    /**
     * 用户登录。
     *
     * @param command
     *            学号登录命令
     * @param response
     *            HTTP响应
     * @return 登录结果
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResult.Login login(AuthCommands.StudentIdLoginCommand command, HttpServletResponse response) {
        User user = authProviderRegistry.authenticate(AuthProviderType.STUDENT_ID, command);
        String csrfToken = authSessionIssuer.issueCookies(user, response);
        log.info("User logged in successfully: {}", command.studentId());
        return new AuthResult.Login(user.getId(), csrfToken);
    }

    /**
     * 用户邮箱登录。
     *
     * @param command
     *            邮箱登录命令
     * @param response
     *            HTTP响应
     * @return 登录结果
     */
    @Override
    @Transactional
    public AuthResult.Login loginWithEmail(AuthCommands.EmailLoginCommand command, HttpServletResponse response) {
        User user = authProviderRegistry.authenticate(AuthProviderType.EMAIL_CODE, command);
        String csrfToken = authSessionIssuer.issueCookies(user, response);
        log.info("User logged in via email successfully: {}", command.email());
        return new AuthResult.Login(user.getId(), csrfToken);
    }

    /**
     * 发送验证码。
     *
     * @param command
     *            发送验证码命令
     */
    @Override
    public void sendVerificationCode(AuthCommands.SendVerificationCodeCommand command) {
        String email = command.email();
        String scene = command.scene() != null ? command.scene() : "login";

        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, scene);
        verificationCodeRepository.save(verifyCodeVO);

        String subject = "蓝网登录验证码";
        String htmlContent = emailVerificationCodeTemplate
                .buildHtml(VerificationCodeScene.LOGIN, verifyCodeVO.getCode());
        messageDispatcher.dispatchAsync(MessageRequest.html(MessageChannel.EMAIL, email, subject, htmlContent));

        log.info("验证码已发送 - email={}, scene={}", email, scene);
    }

    /**
     * 用户登出。
     *
     * @param response
     *            HTTP响应
     */
    @Override
    public void logout(HttpServletResponse response) {
        Long currentUserId = UserCTX.getCurrentUserId();
        if (currentUserId != null) {
            JwtPayload payload = (JwtPayload) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            if (payload != null && payload.getJti() != null) {
                // Token 撤销仍由底层 token 服务负责，门面只处理登出流程编排。
                authTokenService.revokeToken(payload.getJti());
                log.info("Token revoked successfully for jti: {}", payload.getJti());
            }
        }

        cookieService.clearAuthCookies(response);
        UserCTX.clear();
        log.info("User logged out successfully");
    }

    /**
     * 获取当前用户认证信息。
     *
     * @param response
     *            HTTP响应
     * @return 当前用户认证结果
     */
    @Override
    public AuthResult.AuthMe getAuthMe(HttpServletResponse response) {
        if (!UserCTX.isAuthenticated()) {
            return new AuthResult.AuthMe(false, null);
        }

        String csrfToken = csrfTokenService.generateCsrfToken();
        cookieService.setCsrfTokenCookie(response, csrfToken);

        return new AuthResult.AuthMe(true, csrfToken);
    }

    /**
     * 启动GitHub登录。
     *
     * @param callbackBaseUrl
     *            回调基础URL
     * @return GitHub登录URL
     */
    @Override
    public String initiateGithubLogin(String callbackBaseUrl) {
        return githubProvider().initiateLogin(callbackBaseUrl);
    }

    /**
     * 启动GitHub绑定。
     *
     * @param callbackBaseUrl
     *            回调基础URL
     * @return GitHub绑定URL
     */
    @Override
    public String initiateGithubBind(String callbackBaseUrl) {
        return githubProvider().initiateBind(callbackBaseUrl);
    }

    /**
     * 处理GitHub回调。
     *
     * @param code
     *            授权码
     * @param state
     *            状态码
     * @param callbackBaseUrl
     *            回调基础URL
     * @param response
     *            HTTP响应
     */
    @Override
    @Transactional
    public void handleGithubCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response) {
        authProviderRegistry.authenticate(
                AuthProviderType.GITHUB,
                new GitHubCallbackCredential(code, state, callbackBaseUrl, response));
    }

    /**
     * 获取GitHub绑定状态。
     *
     * @return GitHub绑定状态
     */
    @Override
    public String getGithubBindingStatus() {
        return githubProvider().getBindingStatus();
    }

    /**
     * 解绑GitHub。
     */
    @Override
    @Transactional
    public void unbindGithub() {
        githubProvider().unbind();
    }

    private GitHubAuthProvider githubProvider() {
        return authProviderRegistry.get(AuthProviderType.GITHUB, GitHubAuthProvider.class);
    }
}
