package com.bluenet.web.infrastructure.security.jwt;

import com.bluenet.web.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * 预签名上传回调令牌服务。
 * <p>
 * 使用独立的密钥生成和验证专用于上传确认的 JWT Token，与用户认证 Token 隔离。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUploadTokenService {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = (jwtProperties.getSecret() + "_presigned_upload").getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成预签名上传回调令牌。
     *
     * @param fileId
     *            文件 ID
     * @param expectedMd5
     *            预期的文件 MD5
     * @param expiry
     *            令牌过期时间
     * @return JWT 字符串
     */
    public String generateToken(Long fileId, String expectedMd5, Duration expiry) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiry.toMillis());
        return Jwts.builder()
                .subject(fileId.toString())
                .claim("md5", expectedMd5)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析回调令牌。
     *
     * @param token
     *            JWT 字符串
     * @return Claims 对象，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Presigned upload token has expired: {}", e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid presigned upload token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中提取文件 ID。
     */
    public Long getFileId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.warn("Invalid fileId in presigned upload token");
            return null;
        }
    }

    /**
     * 从令牌中提取预期 MD5。
     */
    public String getExpectedMd5(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("md5", String.class);
    }
}
