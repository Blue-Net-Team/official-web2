package com.bluenet.web.infrastructure.github;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

/**
 * GitHub App JWT 生成器，所有 GitHub App 共享。
 * <p>
 * 封装私钥加载（PEM/PKCS#8）与 RS256 JWT 生成逻辑。
 * </p>
 */
@Component
public class GitHubJwtGenerator {

    /**
     * 为指定 App 生成 JWT（有效期 10 分钟，签发时间回拨 60 秒以容忍时钟偏移）。
     *
     * @param appId
     *            GitHub App ID
     * @param privateKey
     *            App 私钥
     * @return JWT 字符串
     */
    public String generateJwt(Long appId, PrivateKey privateKey) {
        Instant now = Instant.now();
        Instant issuedAt = now.minus(60, ChronoUnit.SECONDS);
        Instant expiration = now.plus(10, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(appId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * 从 PEM 文件加载 RSA 私钥（PKCS#8 格式）。
     *
     * @param privateKeyPath
     *            私钥文件路径
     * @return RSA 私钥
     */
    public PrivateKey loadPrivateKey(String privateKeyPath) {
        Path path = Paths.get(privateKeyPath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Private key file not found: " + path);
        }

        try {
            String pem = Files.readString(path);
            String base64 = pem.replaceAll("-----BEGIN[^-]+-----", "")
                    .replaceAll("-----END[^-]+-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from: " + path, e);
        }
    }
}
