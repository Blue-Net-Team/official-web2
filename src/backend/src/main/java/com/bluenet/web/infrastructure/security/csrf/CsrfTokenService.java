package com.bluenet.web.infrastructure.security.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CSRF Token 服务接口 提供 CSRF Token 的生成、存储、验证功能
 */
public interface CsrfTokenService {

    /**
     * 生成新的 CSRF Token
     *
     * @return 随机生成的 CSRF Token 字符串
     */
    String generateCsrfToken();

    /**
     * 设置 CSRF Token 到 Cookie（非 HttpOnly，允许前端读取）
     *
     * @param response
     *            HTTP 响应
     * @param csrfToken
     *            CSRF Token 值
     */
    void setCsrfTokenCookie(HttpServletResponse response, String csrfToken);

    /**
     * 从 Cookie 中获取 CSRF Token
     *
     * @param request
     *            HTTP 请求
     * @return CSRF Token 值，如果不存在则返回 null
     */
    String getCsrfTokenFromCookie(HttpServletRequest request);

    /**
     * 从请求头中获取 CSRF Token
     *
     * @param request
     *            HTTP 请求
     * @return CSRF Token 值，如果不存在则返回 null
     */
    String getCsrfTokenFromHeader(HttpServletRequest request);

    /**
     * 验证 CSRF Token（Double Submit Cookie 模式） 比较 Cookie 中的 token 与 Header 中的 token
     * 是否一致
     *
     * @param request
     *            HTTP 请求
     * @return 验证是否通过
     */
    boolean validateCsrfToken(HttpServletRequest request);

    /**
     * 清除 CSRF Token Cookie
     *
     * @param response
     *            HTTP 响应
     */
    void clearCsrfTokenCookie(HttpServletResponse response);
}
