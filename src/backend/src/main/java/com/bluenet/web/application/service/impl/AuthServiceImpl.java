package com.bluenet.web.application.service.impl;

import java.util.Optional;
import java.util.UUID;

import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.email.EmailSender;
import com.bluenet.web.domain.model.vo.GitHubUserInfo;
import com.bluenet.web.domain.model.vo.OAuthState;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.application.service.AuthService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.infrastructure.security.jwt.JwtPayload;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
import com.bluenet.web.infrastructure.security.util.UserCTX;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证应用服务实现类
 * <p>
 * 应用层服务，负责： - 接收DTO参数并调用领域服务 - 协调多个领域服务完成用例 - 将领域对象转换为DTO返回 - 事务管理 - Cookie 设置
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthDomainService authDomainService;
    private final JwtUtil jwtUtil;
    private final AuthTokenService authTokenService;
    private final UserConverter userConverter;
    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailSender emailSender;
    private final GitHubOAuthService gitHubOAuthService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GitHubOAuthProperties gitHubOAuthProperties;

    private static final long OAUTH_STATE_TTL_SECONDS = 600; // 10 minutes
    private static final String OAUTH_STATE_KEY_PREFIX = "oauth:state:";

    @Override
    @Transactional(readOnly = true)
    public UserAuthResponseDTO login(StudentIdLoginRequestDTO requestDTO, HttpServletResponse response) {
        // 1. 调用领域服务验证登录
        Optional<UserVO> userVOOptional = authDomainService.checkLocalValid(
                requestDTO.getStudentId(),
                requestDTO.getPassword(),
                LocalLoginType.STUDENT_ID);

        // 2. 处理验证结果
        UserVO userVO = userVOOptional.orElseThrow(() -> {
            log.warn("Login failed: invalid credentials - {}", requestDTO.getStudentId());
            return new Unauthorized("学号或密码错误");
        });

        // 3. 生成JWT Token（使用用户ID作为subject）
        String jwtToken = jwtUtil.generateToken(extractUserId(userVO));

        // 4. 调用领域服务写入Token缓存（白名单）
        authTokenService.storeToken(jwtUtil.getJti(jwtToken), extractUserId(userVO));

        // 5. 生成 CSRF Token
        String csrfToken = csrfTokenService.generateCsrfToken();

        // 6. 设置 Cookie（auth_token + csrf_token）
        cookieService.setAuthCookies(response, jwtToken, csrfToken);

        // 7. 构建响应DTO（只返回 csrfToken，JWT 通过 HttpOnly Cookie 传递）
        UserAuthResponseDTO responseDTO = new UserAuthResponseDTO();
        responseDTO.setCsrfToken(csrfToken);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(userVO));

        log.info("User logged in successfully: {}", requestDTO.getStudentId());
        return responseDTO;
    }

    @Override
    @Transactional
    public UserAuthResponseDTO loginWithEmail(String email, String verifyCode, HttpServletResponse response) {
        // 1. 调用领域服务验证邮箱验证码登录
        Optional<UserVO> userVOOptional = authDomainService.checkLocalValid(
                email,
                verifyCode,
                LocalLoginType.EMAIL);

        // 2. 处理验证结果
        UserVO userVO = userVOOptional.orElseThrow(() -> {
            log.warn("Email login failed: invalid credentials - {}", email);
            return new Unauthorized("邮箱或验证码错误");
        });

        // 3. 标记验证码已使用
        verificationCodeRepository.markAsUsed(email, verifyCode);

        // 4. 生成JWT Token并设置Cookie（复用login的步骤3-7）
        String jwtToken = jwtUtil.generateToken(extractUserId(userVO));
        authTokenService.storeToken(jwtUtil.getJti(jwtToken), extractUserId(userVO));
        String csrfToken = csrfTokenService.generateCsrfToken();
        cookieService.setAuthCookies(response, jwtToken, csrfToken);

        // 5. 构建响应
        UserAuthResponseDTO responseDTO = new UserAuthResponseDTO();
        responseDTO.setCsrfToken(csrfToken);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(userVO));

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
        emailSender.sendHtmlAsync(email, subject, htmlContent);

        log.info("验证码已发送 - email={}, scene={}", email, scene);
    }

    private String buildVerificationCodeEmail(String code) {
        return """
                <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                    <h2 style="color:#fa8c16;text-align:center;">蓝网登录验证码</h2>
                    <p style="text-align:center;font-size:14px;color:#666;">您的验证码为：</p>
                    <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">%s</p>
                    <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效，p>
                </div>
                """
                .formatted(code);
    }

    @Override
    public void logout(HttpServletResponse response) {
        // 1. 获取当前用户的 JWT jti
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
            // 从 SecurityContext 获取 JWT payload
            JwtPayload payload = (JwtPayload) org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getCredentials();

            if (payload != null && payload.getJti() != null) {
                // 2. 调用领域服务清除Token缓存
                authTokenService.revokeToken(payload.getJti());
                log.info("Token revoked successfully for jti: {}", payload.getJti());
            }
        }

        // 3. 清除 Cookie
        cookieService.clearAuthCookies(response);

        // 4. 清除当前安全上下文
        UserCTX.clear();

        log.info("User logged out successfully");
    }

    @Override
    public AuthMeResponseDTO getAuthMe(HttpServletResponse response) {
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();

        // 1. 检查是否已登录
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            responseDTO.setAuthenticated(false);
            responseDTO.setUserInfo(null);
            responseDTO.setCsrfToken(null);
            return responseDTO;
        }

        // 2. 生成新的 CSRF Token（刷新）
        String csrfToken = csrfTokenService.generateCsrfToken();

        // 3. 设置 CSRF Token Cookie
        cookieService.setCsrfTokenCookie(response, csrfToken);

        // 4. 构建响应
        responseDTO.setAuthenticated(true);
        responseDTO.setUserInfo(userConverter.convertToUserInfo(currentUser));
        responseDTO.setCsrfToken(csrfToken);

        return responseDTO;
    }

    /**
     * 从UserVO中提取用户ID
     * <p>
     * 注意：当前UserVO中只有学号（studentId），而学号可能是非数字（如包含字母的13位学号）。
     * 为了正确使用数据库自增ID作为JWT的subject，需要在UserVO中添加userId字段。
     * </p>
     *
     * @param userVO
     *            用户领域值对象
     * @return 用户ID
     */
    private Long extractUserId(UserVO userVO) {
        return userVO.getId();
    }

    // ==================== GitHub OAuth ====================

    @Override
    public String initiateGithubLogin(String callbackBaseUrl) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String redirectUri = callbackBaseUrl + "/api/v1/auth/github/callback";
        storeOAuthState(state, "login", null);
        return gitHubOAuthService.buildAuthorizeUrl(state, redirectUri);
    }

    @Override
    public String initiateGithubBind(String callbackBaseUrl) {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new Unauthorized("未登录");
        }

        String state = UUID.randomUUID().toString().replace("-", "");
        String redirectUri = callbackBaseUrl + "/api/v1/auth/github/callback";
        storeOAuthState(state, "bind", currentUser.getId());
        return gitHubOAuthService.buildAuthorizeUrl(state, redirectUri);
    }

    @Override
    @Transactional
    public void handleGithubCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response) {
        // 1. Validate state and determine flow type
        OAuthState oauthState = validateAndConsumeState(state);
        if (oauthState == null) {
            redirectToFrontend(response, "/login", "github=error");
            return;
        }

        // 2. Exchange code for token and get GitHub user info
        String redirectUri = callbackBaseUrl + "/api/v1/auth/github/callback";
        String accessToken;
        GitHubUserInfo githubUser;
        try {
            accessToken = gitHubOAuthService.exchangeCodeForToken(code, redirectUri);
            githubUser = gitHubOAuthService.getUserInfo(accessToken);
        } catch (Exception e) {
            log.error("GitHub OAuth failed", e);
            String redirectPath = "bind".equals(oauthState.getType()) ? "/profile" : "/login";
            redirectToFrontend(response, redirectPath, "github=error");
            return;
        }

        String githubId = String.valueOf(githubUser.getId());

        // 3. Handle based on flow type
        if ("bind".equals(oauthState.getType())) {
            handleBindFlow(oauthState, githubId, githubUser, response);
        } else {
            handleLoginFlow(githubId, githubUser, response);
        }
    }

    private void handleLoginFlow(String githubId, GitHubUserInfo githubUser, HttpServletResponse response) {
        // Find matching user by githubId
        Optional<UserVO> userOpt = userRepository.findByGithubId(githubId);
        if (userOpt.isEmpty()) {
            log.info("GitHub login failed: no user bound to githubId {}", githubId);
            redirectToFrontend(response, "/login", "github=unbound");
            return;
        }

        UserVO userVO = userOpt.get();

        // Update githubUsername if changed
        if (githubUser.getLogin() != null && !githubUser.getLogin().equals(userVO.getGithubUsername())) {
            userRepository.updateGithubBinding(userVO.getId(), githubId, githubUser.getLogin());
        }

        // Set JWT Cookie
        setAuthCookiesForUser(userVO, response);
        log.info("GitHub login success for userId {}", userVO.getId());
        redirectToFrontend(response, "/login", "github=success");
    }

    private void handleBindFlow(OAuthState oauthState, String githubId, GitHubUserInfo githubUser,
            HttpServletResponse response) {
        // Check if already bound to another user
        Optional<UserVO> existingUser = userRepository.findByGithubId(githubId);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(oauthState.getUserId())) {
            log.warn("GitHub bind failed: githubId {} already bound to user {}", githubId, existingUser.get().getId());
            redirectToFrontend(response, "/profile", "github=already_bound");
            return;
        }

        // Bind to user
        userRepository.updateGithubBinding(oauthState.getUserId(), githubId, githubUser.getLogin());
        log.info("GitHub bind success for userId {}", oauthState.getUserId());
        redirectToFrontend(response, "/profile", "github=binding_success");
    }

    @Override
    public String getGithubBindingStatus() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new Unauthorized("未登录");
        }
        return currentUser.getGithubUsername();
    }

    @Override
    @Transactional
    public void unbindGithub() {
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new Unauthorized("未登录");
        }
        if (currentUser.getGithubUsername() == null) {
            throw new BadRequest("未绑定 GitHub 账号");
        }
        userRepository.clearGithubBinding(currentUser.getId());
        log.info("GitHub unbind success for userId {}", currentUser.getId());
    }

    private void storeOAuthState(String state, String type, Long userId) {
        OAuthState oauthState = new OAuthState(type, userId);
        try {
            String stateJson = objectMapper.writeValueAsString(oauthState);
            redisTemplate.opsForValue()
                    .set(
                            OAUTH_STATE_KEY_PREFIX + state,
                            stateJson,
                            java.time.Duration.ofSeconds(OAUTH_STATE_TTL_SECONDS));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OAuth state", e);
        }
    }

    private OAuthState validateAndConsumeState(String state) {
        String key = OAUTH_STATE_KEY_PREFIX + state;
        String stateJson = redisTemplate.opsForValue().getAndDelete(key);
        if (stateJson == null) {
            log.warn("OAuth state not found or expired: {}", state);
            return null;
        }
        try {
            return objectMapper.readValue(stateJson, OAuthState.class);
        } catch (Exception e) {
            log.error("Failed to deserialize OAuth state", e);
            return null;
        }
    }

    private void setAuthCookiesForUser(UserVO userVO, HttpServletResponse response) {
        String jwtToken = jwtUtil.generateToken(userVO.getId());
        authTokenService.storeToken(jwtUtil.getJti(jwtToken), userVO.getId());
        String csrfToken = csrfTokenService.generateCsrfToken();
        cookieService.setAuthCookies(response, jwtToken, csrfToken);
    }

    private void redirectToFrontend(HttpServletResponse response, String path, String query) {
        try {
            String frontendUrl = gitHubOAuthProperties.getFrontendBaseUrl();
            response.sendRedirect(frontendUrl + path + "?" + query);
        } catch (Exception e) {
            log.error("Failed to redirect", e);
        }
    }
}
