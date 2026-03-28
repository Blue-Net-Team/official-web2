package com.bluenet.web.infrastructure.security.cookie;

import com.bluenet.web.infrastructure.config.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cookie 服务实现类 封装认证相关 Cookie 的设置、读取和清除操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CookieServiceImpl implements CookieService {

    private final CookieProperties cookieProperties;

    @Override
    public void setAuthTokenCookie(HttpServletResponse response, String token) {
        String cookieName = cookieProperties.getAuthCookieName();
        String cookieValue = buildCookieValue(cookieName, token, true);
        response.addHeader("Set-Cookie", cookieValue);
        log.debug(
                "Set auth token cookie: name={}, secure={}, domain={}",
                cookieName,
                cookieProperties.isSecure(),
                cookieProperties.getDomain());
    }

    @Override
    public void setCsrfTokenCookie(HttpServletResponse response, String csrfToken) {
        String cookieName = cookieProperties.getCsrfCookieName();
        String cookieValue = buildCookieValue(cookieName, csrfToken, false);
        response.addHeader("Set-Cookie", cookieValue);
        log.debug("Set CSRF token cookie: name={}", cookieName);
    }

    @Override
    public String getAuthTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, cookieProperties.getAuthCookieName());
    }

    @Override
    public String getCsrfTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, cookieProperties.getCsrfCookieName());
    }

    @Override
    public void clearAuthCookies(HttpServletResponse response) {
        // 清除 auth_token Cookie
        String authCookieHeader = buildClearCookieHeader(cookieProperties.getAuthCookieName());
        response.addHeader("Set-Cookie", authCookieHeader);

        // 清除 csrf_token Cookie
        String csrfCookieHeader = buildClearCookieHeader(cookieProperties.getCsrfCookieName());
        response.addHeader("Set-Cookie", csrfCookieHeader);

        log.debug("Cleared auth cookies");
    }

    /**
     * 构建 Cookie 值字符串
     *
     * @param name
     *            Cookie 名称
     * @param value
     *            Cookie 值
     * @param httpOnly
     *            是否设置 HttpOnly
     * @return 完整的 Set-Cookie header 值
     */
    private String buildCookieValue(String name, String value, boolean httpOnly) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        sb.append("; Path=").append(cookieProperties.getPath());
        sb.append("; Max-Age=").append(cookieProperties.getMaxAge());
        sb.append("; SameSite=").append(cookieProperties.getSameSite());

        // HttpOnly 属性
        if (httpOnly) {
            sb.append("; HttpOnly");
        }

        // Secure 属性（生产环境启用）
        if (cookieProperties.isSecure()) {
            sb.append("; Secure");
        }

        // Domain 属性（跨子域时设置）
        String domain = cookieProperties.getDomain();
        if (domain != null && !domain.isEmpty()) {
            sb.append("; Domain=").append(domain);
        }

        return sb.toString();
    }

    /**
     * 构建清除 Cookie 的 header 值
     *
     * @param name
     *            Cookie 名称
     * @return 设置 Max-Age=0 的 Set-Cookie header 值
     */
    private String buildClearCookieHeader(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=;");
        sb.append("; Path=").append(cookieProperties.getPath());
        sb.append("; Max-Age=0");

        // 清除时也需要匹配 Domain
        String domain = cookieProperties.getDomain();
        if (domain != null && !domain.isEmpty()) {
            sb.append("; Domain=").append(domain);
        }

        return sb.toString();
    }

    /**
     * 从请求中获取指定名称的 Cookie 值
     *
     * @param request
     *            HTTP 请求
     * @param cookieName
     *            Cookie 名称
     * @return Cookie 值，如果不存在则返回 null
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
