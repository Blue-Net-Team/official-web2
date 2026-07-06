package com.bluenet.web.application.service.auth.session;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 统一签发登录会话，避免不同登录入口重复生成 JWT、CSRF 和 Cookie。
 */
@RequiredArgsConstructor
public class AuthSessionIssuer {
    private final JwtUtil jwtUtil;
    private final AuthTokenService authTokenService;
    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;

    /**
     * 只签发 Cookie，适用于 GitHub OAuth 回调后重定向场景。
     *
     * @param user
     *            已解析出的登录用户实体。
     * @param response
     *            HTTP 响应。
     * @return 新生成的 CSRF Token。
     */
    public String issueCookies(User user, HttpServletResponse response) {
        String jwtToken = jwtUtil.generateToken(user.getId());
        authTokenService.storeToken(jwtUtil.getJti(jwtToken), user.getId());
        String csrfToken = csrfTokenService.generateCsrfToken();
        cookieService.setAuthCookies(response, jwtToken, csrfToken);
        return csrfToken;
    }
}
