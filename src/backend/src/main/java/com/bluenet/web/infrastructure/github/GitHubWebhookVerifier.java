package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * GitHub Webhook 签名验证器。
 * <p>
 * 使用 HMAC-SHA256 算法验证 GitHub Webhook 请求的签名， 防止伪造请求。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubWebhookVerifier {

    private static final String SIGNATURE_PREFIX = "sha256=";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final GitHubAppProperties properties;

    /**
     * 验证 Webhook 请求的签名。
     *
     * @param payload
     *            请求原始 body 字符串
     * @param signatureHeader
     *            X-Hub-Signature-256 header 值，格式为 "sha256=<hex>"
     * @throws IllegalArgumentException
     *             签名验证失败或配置未启用
     */
    public void verify(String payload, String signatureHeader) {
        if (!properties.isWebhookEnabled()) {
            log.warn("GitHub Webhook 未启用，拒绝请求");
            throw new IllegalArgumentException("GitHub Webhook 未配置");
        }

        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("GitHub Webhook 请求缺少 X-Hub-Signature-256 header");
            throw new IllegalArgumentException("缺少签名 header");
        }

        if (!signatureHeader.startsWith(SIGNATURE_PREFIX)) {
            log.warn("GitHub Webhook 签名格式无效: {}", signatureHeader);
            throw new IllegalArgumentException("签名格式无效");
        }

        String receivedSignature = signatureHeader.substring(SIGNATURE_PREFIX.length());
        String computedSignature = computeSignature(payload);

        if (!constantTimeEquals(receivedSignature, computedSignature)) {
            log.warn("GitHub Webhook 签名验证失败");
            throw new IllegalArgumentException("签名验证失败");
        }

        log.debug("GitHub Webhook 签名验证通过");
    }

    /**
     * 使用配置的 secret 计算 HMAC-SHA256 签名。
     */
    String computeSignature(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(signatureBytes);
        } catch (Exception e) {
            throw new IllegalStateException("计算 HMAC-SHA256 签名失败", e);
        }
    }

    /**
     * 常量时间比较，防止时序攻击。
     */
    boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
