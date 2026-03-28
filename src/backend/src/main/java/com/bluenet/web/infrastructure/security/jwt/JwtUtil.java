package com.bluenet.web.infrastructure.security.jwt;

import com.bluenet.web.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类 提供JWT Token的生成、解析和验证功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    /**
     * 初始化JWT密钥
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     *
     * @param userId
     *            用户ID
     * @return 生成的JWT字符串
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(userId.toString())
                .id(jti)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param jwtString
     *            JWT字符串
     * @return JwtPayload对象，解析失败返回null
     */
    public JwtPayload parseToken(String jwtString) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtString).getPayload();

            JwtPayload payload = new JwtPayload();
            payload.setUserId(Long.parseLong(claims.getSubject()));
            payload.setJti(claims.getId());
            payload.setIssuedAt(claims.getIssuedAt().getTime() / 1000);
            payload.setExpiration(claims.getExpiration().getTime() / 1000);

            return payload;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or empty: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT字符串中提取JTI（JWT ID）
     *
     * @param jwtString
     *            JWT字符串
     * @return JTI字符串，提取失败返回null
     */
    public String getJti(String jwtString) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtString).getPayload();
            return claims.getId();
        } catch (JwtException e) {
            log.warn("Failed to extract JTI from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT Token是否有效
     *
     * @param jwtString
     *            JWT字符串
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String jwtString) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtString);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
