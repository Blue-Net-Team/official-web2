package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VerifyCode} 实体单元测试。
 */
@DisplayName("VerifyCode 实体单元测试")
class VerifyCodeTest {

    @Test
    @DisplayName("isUsed: 未使用时返回 false")
    void isUsed_notUsed_shouldReturnFalse() {
        VerifyCode code = VerifyCode.create("test@example.com", "123456", LocalDateTime.now().plusMinutes(5), "LOGIN");
        assertFalse(code.isUsed());
    }

    @Test
    @DisplayName("isUsed: 已使用时返回 true")
    void isUsed_used_shouldReturnTrue() {
        VerifyCode code = VerifyCode.reconstruct(
                1L,
                "test@example.com",
                "123456",
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now(),
                "LOGIN");
        assertTrue(code.isUsed());
    }

    @Test
    @DisplayName("isExpired: 未过期时返回 false")
    void isExpired_notExpired_shouldReturnFalse() {
        VerifyCode code = VerifyCode.create("test@example.com", "123456", LocalDateTime.now().plusMinutes(5), "LOGIN");
        assertFalse(code.isExpired());
    }

    @Test
    @DisplayName("isExpired: 已过期时返回 true")
    void isExpired_expired_shouldReturnTrue() {
        VerifyCode code = VerifyCode.create("test@example.com", "123456", LocalDateTime.now().minusMinutes(1), "LOGIN");
        assertTrue(code.isExpired());
    }
}
