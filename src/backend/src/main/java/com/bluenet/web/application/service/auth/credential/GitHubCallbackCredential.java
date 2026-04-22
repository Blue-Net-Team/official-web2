package com.bluenet.web.application.service.auth.credential;

import jakarta.servlet.http.HttpServletResponse;

/**
 * GitHub OAuth 回调认证上下文。
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
public record GitHubCallbackCredential(
        String code,
        String state,
        String callbackBaseUrl,
        HttpServletResponse response) {
}
