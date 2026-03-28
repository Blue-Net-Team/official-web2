package com.bluenet.web.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cookie 配置属性 支持开发和生产环境的不同配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    /**
     * Cookie Domain，跨子域共享时设置为 .example.com 开发环境通常不设置
     */
    private String domain;

    /**
     * 是否启用 Secure 属性 生产环境应为 true（需要 HTTPS） 开发环境可以为 false
     */
    private boolean secure = false;

    /**
     * SameSite 属性 可选值: Strict, Lax, None 默认 Lax，平衡安全性和可用性
     */
    private String sameSite = "Lax";

    /**
     * JWT Cookie 名称
     */
    private String authCookieName = "auth_token";

    /**
     * CSRF Token Cookie 名称
     */
    private String csrfCookieName = "csrf_token";

    /**
     * Cookie 有效期（秒） 默认 12 小时，与 JWT 过期时间一致
     */
    private int maxAge = 43200;

    /**
     * Cookie 路径
     */
    private String path = "/";
}
