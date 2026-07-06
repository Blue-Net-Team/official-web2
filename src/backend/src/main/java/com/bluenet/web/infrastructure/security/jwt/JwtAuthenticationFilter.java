package com.bluenet.web.infrastructure.security.jwt;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.CookieProperties;
import com.bluenet.web.infrastructure.config.FailAuthEntryPoint;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * JWT认证过滤器 优先从 Cookie 中提取 JWT Token，fallback 到 Authorization Header（过渡期）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthTokenService authTokenService;
    private final UserRepository userRepository;
    private final FailAuthEntryPoint failAuthEntryPoint;
    private final CookieProperties cookieProperties;
    private final PermissionCache permissionCache;
    private final RoleTypeResolver roleTypeResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. 从请求头中提取JWT令牌
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                // 2. 解析JWT获取载荷信息
                JwtPayload payload = jwtUtil.parseToken(jwt);

                if (payload != null) {
                    // 3. 验证Token是否在白名单中
                    Optional<Long> userId = authTokenService.validateToken(payload.getJti());

                    if (userId.isPresent() && userId.get().equals(payload.getUserId())) {
                        // 取出用户
                        Optional<User> userOpt = userRepository.findById(userId.get());
                        if (userOpt.isEmpty()) {
                            log.warn("User not found for userId: {}", userId.get());
                            failAuthEntryPoint.commence(request, response, new GlobalException("用户不存在"));
                            return; // 校验失败后直接返回
                        }

                        User user = userOpt.get();
                        SecurityPrincipal principal = new SecurityPrincipal(
                                user,
                                roleTypeResolver.resolve(user.getRoleId()),
                                permissionCache.getPermissionsByRole(user.getRoleId()));

                        // 4. 设置Spring Security上下文
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal, payload, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 5. 设置自定义SecurityContext
                        UserCTX.setPrincipal(principal);

                        log.debug("JWT authenticated for user: {}", payload.getUserId());
                    } else {
                        // 如果白名单没有，说明已经退出，非法token
                        log.warn("Token not found in whitelist or userId mismatch: {}", payload.getJti());
                        failAuthEntryPoint.commence(request, response, new Unauthorized("无效的Token"));
                        return; // 校验失败后直接返回
                    }
                } else {
                    log.debug("Failed to parse JWT token");
                    failAuthEntryPoint.commence(request, response, new Unauthorized("无效的Token"));
                    return; // 校验失败后直接返回
                }
            }

            // 继续过滤器链
            filterChain.doFilter(request, response);

        } finally {
            // 6. 清理SecurityContext，防止内存泄漏
            UserCTX.clear();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 从请求中提取JWT令牌 优先从 Cookie 中提取，fallback 到 Authorization Header（过渡期）
     *
     * @param request
     *            HTTP请求
     * @return JWT令牌，如果没有则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. 优先从 Cookie 中提取
        String tokenFromCookie = extractJwtFromCookie(request);
        if (tokenFromCookie != null) {
            log.debug("JWT extracted from cookie");
            return tokenFromCookie;
        }

        // 2. Fallback: 从 Authorization Header 提取（过渡期保留）
        String tokenFromHeader = extractJwtFromHeader(request);
        if (tokenFromHeader != null) {
            log.debug("JWT extracted from Authorization header (fallback)");
            return tokenFromHeader;
        }

        return null;
    }

    /**
     * 从 Cookie 中提取 JWT Token
     *
     * @param request
     *            HTTP 请求
     * @return JWT Token，如果不存在则返回 null
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String authCookieName = cookieProperties.getAuthCookieName();
        for (Cookie cookie : cookies) {
            if (authCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 从 Authorization Header 中提取 JWT Token（过渡期保留）
     *
     * @param request
     *            HTTP 请求
     * @return JWT Token，如果不存在则返回 null
     */
    private String extractJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
