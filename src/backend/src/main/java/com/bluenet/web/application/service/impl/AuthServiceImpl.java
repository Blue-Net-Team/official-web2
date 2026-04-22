package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.service.AuthService;
import com.bluenet.web.application.service.auth.credential.EmailCodeCredential;
import com.bluenet.web.application.service.auth.credential.GitHubCallbackCredential;
import com.bluenet.web.application.service.auth.provider.EmailCodeLoginProvider;
import com.bluenet.web.application.service.auth.provider.GitHubAuthProvider;
import com.bluenet.web.application.service.auth.provider.StudentIdLoginProvider;
import com.bluenet.web.application.service.auth.session.AuthSessionIssuer;
import com.bluenet.web.application.service.auth.strategy.AuthProviderRegistry;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.infrastructure.security.oauth.OAuthStateStore;
import com.bluenet.web.domain.model.vo.UserVO;
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
 * 认证应用服务门面，只编排认证用例，不直接处理具体凭证校验和 OAuth 细节。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserConverter userConverter;
    private final AuthTokenService authTokenService;
    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageDispatcher messageDispatcher;
    private final AuthSessionIssuer authSessionIssuer;
    private final AuthProviderRegistry authProviderRegistry;

    /**
     * 保持原依赖注入入口，内部组合更细粒度 provider，避免应用服务继续膨胀。
     */
    public AuthServiceImpl(
            AuthDomainService authDomainService,
            JwtUtil jwtUtil,
            AuthTokenService authTokenService,
            UserConverter userConverter,
            CookieService cookieService,
            CsrfTokenService csrfTokenService,
            VerificationCodeDomainService verificationCodeDomainService,
            VerificationCodeRepository verificationCodeRepository,
            MessageDispatcher messageDispatcher,
            GitHubOAuthService gitHubOAuthService,
            UserRepository userRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            GitHubOAuthProperties gitHubOAuthProperties) {
        this.userConverter = userConverter;
        this.authTokenService = authTokenService;
        this.cookieService = cookieService;
        this.csrfTokenService = csrfTokenService;
        this.verificationCodeDomainService = verificationCodeDomainService;
        this.verificationCodeRepository = verificationCodeRepository;
        this.messageDispatcher = messageDispatcher;
        this.authSessionIssuer = new AuthSessionIssuer(
                jwtUtil,
                authTokenService,
                userConverter,
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

    @Override
    @Transactional(readOnly = true)
    public UserAuthResponseDTO login(StudentIdLoginRequestDTO requestDTO, HttpServletResponse response) {
        UserVO userVO = authProviderRegistry.authenticate(AuthProviderType.STUDENT_ID, requestDTO);
        UserAuthResponseDTO responseDTO = authSessionIssuer.issue(userVO, response);
        log.info("User logged in successfully: {}", requestDTO.getStudentId());
        return responseDTO;
    }

    @Override
    @Transactional
    public UserAuthResponseDTO loginWithEmail(String email, String verifyCode, HttpServletResponse response) {
        UserVO userVO = authProviderRegistry
                .authenticate(AuthProviderType.EMAIL_CODE, new EmailCodeCredential(email, verifyCode));
        UserAuthResponseDTO responseDTO = authSessionIssuer.issue(userVO, response);
        log.info("User logged in via email successfully: {}", email);
        return responseDTO;
    }

    @Override
    public void sendVerificationCode(SendVerificationCodeRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        String scene = requestDTO.getScene() != null ? requestDTO.getScene() : "login";

        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, scene);
        verificationCodeRepository.save(verifyCodeVO);

        String subject = "蓝网登录验证码";
        String htmlContent = buildVerificationCodeEmail(verifyCodeVO.getCode());
        messageDispatcher.dispatchAsync(MessageRequest.html(MessageChannel.EMAIL, email, subject, htmlContent));

        log.info("验证码已发送 - email={}, scene={}", email, scene);
    }

    /**
     * 构建验证码邮件 HTML，发送仍复用现有 MessageDispatcher。
     */
    private String buildVerificationCodeEmail(String code) {
        return """
                <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                    <h2 style="color:#fa8c16;text-align:center;">蓝网登录验证码</h2>
                    <p style="text-align:center;font-size:14px;color:#666;">您的验证码为：</p>
                    <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">%s</p>
                    <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效。</p>
                </div>
                """
                .formatted(code);
    }

    @Override
    public void logout(HttpServletResponse response) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
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

    @Override
    public AuthMeResponseDTO getAuthMe(HttpServletResponse response) {
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();

        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setUserInfo(null);
            responseDTO.setCsrfToken(null);
            return responseDTO;
        }

        String csrfToken = csrfTokenService.generateCsrfToken();
        cookieService.setCsrfTokenCookie(response, csrfToken);

        responseDTO.setAuthenticated(true);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(currentUser));
        responseDTO.setCsrfToken(csrfToken);
        return responseDTO;
    }

    @Override
    public String initiateGithubLogin(String callbackBaseUrl) {
        return githubProvider().initiateLogin(callbackBaseUrl);
    }

    @Override
    public String initiateGithubBind(String callbackBaseUrl) {
        return githubProvider().initiateBind(callbackBaseUrl);
    }

    @Override
    @Transactional
    public void handleGithubCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response) {
        authProviderRegistry.authenticate(
                AuthProviderType.GITHUB,
                new GitHubCallbackCredential(code, state, callbackBaseUrl, response));
    }

    @Override
    public String getGithubBindingStatus() {
        return githubProvider().getBindingStatus();
    }

    @Override
    @Transactional
    public void unbindGithub() {
        githubProvider().unbind();
    }

    private GitHubAuthProvider githubProvider() {
        return authProviderRegistry.get(AuthProviderType.GITHUB, GitHubAuthProvider.class);
    }
}
