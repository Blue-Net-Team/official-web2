package com.bluenet.web.infrastructure.security.annotation;

/**
 * 访问级别枚举 定义三种访问控制级别
 */
public enum AccessLevel {
    /**
     * 公开访问 - 无需认证和授权
     */
    PUBLIC,

    /**
     * 登录用户 - 需要有效的登录凭证
     */
    AUTHENTICATED,

    /**
     * 受保护 - 需要特定权限
     */
    PROTECTED
}
