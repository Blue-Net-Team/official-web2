package com.bluenet.web.application.service.auth.provider;

import java.util.Optional;
import java.util.UUID;

import com.bluenet.web.application.service.auth.credential.GitHubCallbackCredential;
import com.bluenet.web.application.service.auth.session.AuthSessionIssuer;
import com.bluenet.web.application.service.auth.strategy.AbstractAuthProvider;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.GitHubUserInfo;
import com.bluenet.web.domain.model.vo.OAuthState;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.infrastructure.config.GitHubOAuthProperties;
import com.bluenet.web.infrastructure.security.oauth.OAuthStateStore;
import com.bluenet.web.infrastructure.security.util.UserCTX;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * GitHub OAuth 登录、绑定和解绑 provider。
 */
@Slf4j
public class GitHubAuthProvider extends AbstractAuthProvider<GitHubCallbackCredential, Void> {
    private final GitHubOAuthService gitHubOAuthService;
    private final UserRepository userRepository;
    private final GitHubOAuthProperties gitHubOAuthProperties;
    private final OAuthStateStore oauthStateStore;
    private final AuthSessionIssuer authSessionIssuer;

    public GitHubAuthProvider(
            GitHubOAuthService gitHubOAuthService,
            UserRepository userRepository,
            GitHubOAuthProperties gitHubOAuthProperties,
            OAuthStateStore oauthStateStore,
            AuthSessionIssuer authSessionIssuer) {
        super(AuthProviderType.GITHUB);
        this.gitHubOAuthService = gitHubOAuthService;
        this.userRepository = userRepository;
        this.gitHubOAuthProperties = gitHubOAuthProperties;
        this.oauthStateStore = oauthStateStore;
        this.authSessionIssuer = authSessionIssuer;
    }

    /**
     * 初始化 GitHub 登录流程。
     *
     * @param callbackBaseUrl
     *            后端回调地址根路径。
     * @return GitHub 授权地址。
     */
    public String initiateLogin(String callbackBaseUrl) {
        String state = newState();
        oauthStateStore.store(state, "login", null);
        return gitHubOAuthService.buildAuthorizeUrl(state, buildRedirectUri(callbackBaseUrl));
    }

    /**
     * 初始化 GitHub 绑定流程。
     *
     * @param callbackBaseUrl
     *            后端回调地址根路径。
     * @return GitHub 授权地址。
     */
    public String initiateBind(String callbackBaseUrl) {
        User currentUser = requireCurrentUser();
        String state = newState();
        oauthStateStore.store(state, "bind", currentUser.getId());
        return gitHubOAuthService.buildAuthorizeUrl(state, buildRedirectUri(callbackBaseUrl));
    }

    /**
     * 处理 GitHub OAuth 回调。
     *
     * @param code
     *            GitHub 授权码。
     * @param state
     *            OAuth state。
     * @param callbackBaseUrl
     *            后端回调地址根路径。
     * @param response
     *            HTTP 响应。
     */
    public void handleCallback(String code, String state, String callbackBaseUrl, HttpServletResponse response) {
        authenticate(new GitHubCallbackCredential(code, state, callbackBaseUrl, response));
    }

    /**
     * 执行 GitHub OAuth 回调认证策略。
     *
     * @param credential
     *            GitHub OAuth 回调上下文。
     * @return GitHub 回调通过重定向返回前端，因此固定返回 null。
     */
    @Override
    public Void authenticate(GitHubCallbackCredential credential) {
        String code = credential.code();
        String state = credential.state();
        String callbackBaseUrl = credential.callbackBaseUrl();
        HttpServletResponse response = credential.response();
        OAuthState oauthState = oauthStateStore.consume(state);
        if (oauthState == null) {
            redirectToFrontend(response, "/login", "github=error");
            return null;
        }

        GitHubUserInfo githubUser;
        try {
            String accessToken = gitHubOAuthService.exchangeCodeForToken(code, buildRedirectUri(callbackBaseUrl));
            githubUser = gitHubOAuthService.getUserInfo(accessToken);
        } catch (Exception e) {
            log.error("GitHub OAuth failed", e);
            redirectToFrontend(response, "bind".equals(oauthState.getType()) ? "/profile" : "/login", "github=error");
            return null;
        }

        String githubId = String.valueOf(githubUser.getId());
        if ("bind".equals(oauthState.getType())) {
            handleBindFlow(oauthState, githubId, githubUser, response);
        } else {
            handleLoginFlow(githubId, githubUser, response);
        }
        return null;
    }

    /**
     * 获取当前用户绑定的 GitHub 用户名。
     *
     * @return GitHub 用户名，未绑定时为 null。
     */
    public String getBindingStatus() {
        return requireCurrentUser().getGithubUsername();
    }

    /**
     * 解绑当前用户的 GitHub 账号。
     */
    public void unbind() {
        User currentUser = requireCurrentUser();
        if (currentUser.getGithubUsername() == null) {
            throw new BadRequest("未绑定 GitHub 账号");
        }
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        user.clearGithubBinding();
        userRepository.save(user);
        log.info("GitHub unbind success for userId {}", user.getId());
    }

    private void handleLoginFlow(String githubId, GitHubUserInfo githubUser, HttpServletResponse response) {
        Optional<User> userOpt = userRepository.findByGithubId(githubId);
        if (userOpt.isEmpty()) {
            log.info("GitHub login failed: no user bound to githubId {}", githubId);
            redirectToFrontend(response, "/login", "github=unbound");
            return;
        }

        User user = userOpt.get();
        if (githubUser.getLogin() != null && !githubUser.getLogin().equals(user.getGithubUsername())) {
            user.bindGithub(githubId, githubUser.getLogin());
            userRepository.save(user);
        }

        authSessionIssuer.issueCookies(user, response);
        log.info("GitHub login success for userId {}", user.getId());
        redirectToFrontend(response, "/login", "github=success");
    }

    private void handleBindFlow(OAuthState oauthState, String githubId, GitHubUserInfo githubUser,
            HttpServletResponse response) {
        Optional<User> existingUser = userRepository.findByGithubId(githubId);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(oauthState.getUserId())) {
            log.warn("GitHub bind failed: githubId {} already bound to user {}", githubId, existingUser.get().getId());
            redirectToFrontend(response, "/profile", "github=already_bound");
            return;
        }

        User user = userRepository.findById(oauthState.getUserId())
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        user.bindGithub(githubId, githubUser.getLogin());
        userRepository.save(user);
        log.info("GitHub bind success for userId {}", user.getId());
        redirectToFrontend(response, "/profile", "github=binding_success");
    }

    private User requireCurrentUser() {
        User currentUser = UserCTX.getCurrentUser();
        if (currentUser == null) {
            throw new Unauthorized("未登录");
        }
        return currentUser;
    }

    private String newState() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildRedirectUri(String callbackBaseUrl) {
        return callbackBaseUrl + "/api/v1/auth/github/callback";
    }

    private void redirectToFrontend(HttpServletResponse response, String path, String query) {
        try {
            response.sendRedirect(gitHubOAuthProperties.getFrontendBaseUrl() + path + "?" + query);
        } catch (Exception e) {
            log.error("Failed to redirect", e);
        }
    }
}
