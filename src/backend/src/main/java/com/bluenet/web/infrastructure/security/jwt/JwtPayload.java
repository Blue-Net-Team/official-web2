package com.bluenet.web.infrastructure.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT载荷数据类 用于存储JWT Token中的声明信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtPayload {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * JWT唯一标识符（JWT ID）
     */
    private String jti;

    /**
     * 签发时间（Unix时间戳，秒）
     */
    private Long issuedAt;

    /**
     * 过期时间（Unix时间戳，秒）
     */
    private Long expiration;
}
