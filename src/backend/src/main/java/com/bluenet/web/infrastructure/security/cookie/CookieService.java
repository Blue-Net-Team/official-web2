package com.bluenet.web.infrastructure.security.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cookie 服务接口 封装认证相关 Cookie 的设置、读取和清除操作
 */
public interface CookieService {

    /**
     * 设置认证 Token Cookie（HttpOnly）
     *
     * @param response
     *            HTTP 响应
     * @param token
     *            JWT Token 值
     */
    void setAuthTokenCookie(HttpServletResponse response, String token);

    /**
     * 设置 CSRF Token Cookie（非 HttpOnly，允许 JS 读取） 同时用于认证后设置新 token
     *
     * @param response
     *            HTTP 响应
     * @param csrfToken
     *            CSRF Token 值
     */
    void setCsrfTokenCookie(HttpServletResponse response, String csrfToken);

    /**
     * 设置认证相关 Cookie（auth_token + csrf_token） 便捷方法，登录时调用
     *
     * @param response
     *            HTTP 响应
     * @param authToken
     *            JWT Token 值
     * @param csrfToken
     *            CSRF Token 值
     */
    default void setAuthCookies(HttpServletResponse response, String authToken, String csrfToken) {
        setAuthTokenCookie(response, authToken);
        setCsrfTokenCookie(response, csrfToken);
    }

    /**
     * 从 Cookie 中读取认证 Token
     *
     * @param request
     *            HTTP 请求
     * @return JWT Token 值，如果不存在则返回 null
     */
    String getAuthTokenFromCookie(HttpServletRequest request);

    /**
     * 从 Cookie 中读取 CSRF Token
     *
     * @param request
     *            HTTP 请求
     * @return CSRF Token 值，如果不存在则返回 null
     */
    String getCsrfTokenFromCookie(HttpServletRequest request);

    /**
     * 清除认证相关 Cookie（登出时调用）
     *
     * @param response
     *            HTTP 响应
     */
    void clearAuthCookies(HttpServletResponse response);
}
