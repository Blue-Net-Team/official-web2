package com.bluenet.web.infrastructure.github;

import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("GitHubWebhookVerifier 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubWebhookVerifierTest {

    private static final String TEST_SECRET = "my-webhook-secret-123";
    private static final String TEST_PAYLOAD = "{\"action\":\"closed\",\"issue\":{\"number\":42}}";

    @Mock
    private GitHubAppProperties properties;

    private GitHubWebhookVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new GitHubWebhookVerifier(properties);
    }

    @Nested
    @DisplayName("verify 方法测试")
    class VerifyTest {

        @Test
        @DisplayName("TC-001: 正确签名应通过验证")
        void verify_correctSignature_shouldPass() throws Exception {
            when(properties.isWebhookEnabled()).thenReturn(true);
            when(properties.getWebhookSecret()).thenReturn(TEST_SECRET);

            String signature = computeExpectedSignature(TEST_PAYLOAD, TEST_SECRET);
            String header = "sha256=" + signature;

            assertDoesNotThrow(() -> verifier.verify(TEST_PAYLOAD, header));
        }

        @Test
        @DisplayName("TC-002: 错误签名应抛出异常")
        void verify_wrongSignature_shouldThrowException() {
            when(properties.isWebhookEnabled()).thenReturn(true);
            when(properties.getWebhookSecret()).thenReturn(TEST_SECRET);

            String wrongSignature = "sha256=" + "0000000000000000000000000000000000000000000000000000000000000000";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> verifier.verify(TEST_PAYLOAD, wrongSignature));
            assertEquals("签名验证失败", exception.getMessage());
        }

        @Test
        @DisplayName("TC-003: 缺失签名 header 应抛出异常")
        void verify_missingHeader_shouldThrowException() {
            when(properties.isWebhookEnabled()).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> verifier.verify(TEST_PAYLOAD, null));
            assertEquals("缺少签名 header", exception.getMessage());
        }

        @Test
        @DisplayName("TC-004: 配置未启用时应抛出异常")
        void verify_disabled_shouldThrowException() {
            when(properties.isWebhookEnabled()).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> verifier.verify(TEST_PAYLOAD, "sha256=abc"));
            assertEquals("GitHub Webhook 未配置", exception.getMessage());
        }

        @Test
        @DisplayName("TC-005: 签名格式无效时应抛出异常")
        void verify_invalidSignatureFormat_shouldThrowException() {
            when(properties.isWebhookEnabled()).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> verifier.verify(TEST_PAYLOAD, "invalid-format"));
            assertEquals("签名格式无效", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("computeSignature 方法测试")
    class ComputeSignatureTest {

        @Test
        @DisplayName("相同 payload 和 secret 应生成相同签名")
        void computeSignature_sameInput_shouldProduceSameOutput() {
            when(properties.getWebhookSecret()).thenReturn(TEST_SECRET);

            String sig1 = verifier.computeSignature(TEST_PAYLOAD);
            String sig2 = verifier.computeSignature(TEST_PAYLOAD);

            assertEquals(sig1, sig2);
            assertEquals(64, sig1.length()); // SHA-256 hex = 64 chars
        }
    }

    @Nested
    @DisplayName("constantTimeEquals 方法测试")
    class ConstantTimeEqualsTest {

        @Test
        @DisplayName("相同字符串应返回 true")
        void constantTimeEquals_sameStrings_shouldReturnTrue() {
            assertTrue(verifier.constantTimeEquals("abc", "abc"));
        }

        @Test
        @DisplayName("不同字符串应返回 false")
        void constantTimeEquals_differentStrings_shouldReturnFalse() {
            assertFalse(verifier.constantTimeEquals("abc", "def"));
        }
    }

    private String computeExpectedSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
