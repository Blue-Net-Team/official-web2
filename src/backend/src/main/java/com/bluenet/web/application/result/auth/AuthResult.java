package com.bluenet.web.application.result.auth;

/**
 * 认证聚合的应用层结果对象。
 * <p>
 * 封装了认证相关操作返回给 API 层的数据。
 * </p>
 */
public final class AuthResult {

    private AuthResult() {
        // 工具类，禁止实例化
    }

    /**
     * 登录结果。
     */
    public record Login(
            /** 登录用户ID */
            Long userId,
            /** CSRF令牌 */
            String csrfToken) {
    }

    /**
     * 获取当前登录状态结果。
     */
    public record AuthMe(
            /** 是否已认证 */
            boolean authenticated,
            /** CSRF令牌 */
            String csrfToken) {
    }
}
