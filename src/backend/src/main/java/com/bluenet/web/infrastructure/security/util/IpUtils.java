package com.bluenet.web.infrastructure.security.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * IP 地址提取工具
 * <p>
 * 支持反向代理场景，按优先级提取真实客户端 IP
 * </p>
 */
@UtilityClass
public class IpUtils {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String IP_SEPARATOR = ",";

    /**
     * 从请求中提取客户端真实 IP 地址
     * <p>
     * 优先级：X-Forwarded-For → X-Real-IP → getRemoteAddr()
     * </p>
     */
    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String ip = xForwardedFor.split(IP_SEPARATOR)[0].trim();
            if (!ip.isBlank()) {
                return ip;
            }
        }

        String xRealIp = request.getHeader(X_REAL_IP);
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}
