package com.bluenet.web.infrastructure.security.auth;

import java.util.Optional;

/**
 * 认证Token服务接口 管理JWT Token的白名单存储、验证和吊销
 */
public interface AuthTokenService {

    /**
     * 存储Token到白名单
     *
     * @param jti
     *            JWT唯一标识符
     * @param userId
     *            用户ID
     */
    void storeToken(String jti, Long userId);

    /**
     * 验证Token是否在白名单中
     *
     * @param jti
     *            JWT唯一标识符
     * @return Optional包含用户ID，如果不存在则返回empty
     */
    Optional<Long> validateToken(String jti);

    /**
     * 吊销Token（从白名单中移除）
     *
     * @param jti
     *            JWT唯一标识符
     * @return 是否成功移除
     */
    boolean revokeToken(String jti);

    /**
     * 吊销用户的所有Token
     *
     * @param userId
     *            用户ID
     */
    void revokeAllUserTokens(Long userId);
}
