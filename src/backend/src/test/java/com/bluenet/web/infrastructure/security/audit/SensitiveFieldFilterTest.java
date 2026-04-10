package com.bluenet.web.infrastructure.security.audit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SensitiveFieldFilter 单元测试")
class SensitiveFieldFilterTest {

    @Test
    @DisplayName("password 字段应被脱敏为 ***")
    void password_shouldBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("studentId", "2024001001");
        params.put("password", "secretHash123");

        String result = SensitiveFieldFilter.maskSensitiveFields(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertTrue(result.contains("2024001001"));
        assertFalse(result.contains("secretHash123"));
    }

    @Test
    @DisplayName("newPassword 和 confirmPassword 都应被脱敏")
    void passwordVariants_shouldBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("newPassword", "newPass123");
        params.put("confirmPassword", "newPass123");
        params.put("resetToken", "token-abc");

        String result = SensitiveFieldFilter.maskSensitiveFields(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertFalse(result.contains("newPass123"));
        assertFalse(result.contains("token-abc"));
    }

    @Test
    @DisplayName("verifyCode 字段应被脱敏")
    void verifyCode_shouldBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("email", "test@test.com");
        params.put("verifyCode", "123456");

        String result = SensitiveFieldFilter.maskSensitiveFields(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertTrue(result.contains("test@test.com"));
        assertFalse(result.contains("123456"));
    }

    @Test
    @DisplayName("无敏感字段的请求应完整记录")
    void normalRequest_shouldNotBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "张三");
        params.put("age", 20);

        String result = SensitiveFieldFilter.maskSensitiveFields(params);

        assertNotNull(result);
        assertTrue(result.contains("张三"));
        assertTrue(result.contains("20"));
        assertFalse(result.contains("***"));
    }

    @Test
    @DisplayName("嵌套 Map 中的敏感字段也应被脱敏")
    void nestedSensitiveFields_shouldBeMasked() {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("password", "innerPass");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("studentId", "123");
        params.put("credentials", inner);

        String result = SensitiveFieldFilter.maskSensitiveFields(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertFalse(result.contains("innerPass"));
        assertTrue(result.contains("123"));
    }

    @Test
    @DisplayName("null 或空 Map 应返回 null")
    void nullOrEmpty_shouldReturnNull() {
        assertTrue(SensitiveFieldFilter.maskSensitiveFields(null) == null);
        assertTrue(SensitiveFieldFilter.maskSensitiveFields(new LinkedHashMap<>()) == null);
    }

    @Test
    @DisplayName("isSensitive 应正确判断敏感字段")
    void isSensitive_shouldIdentifySensitiveFields() {
        assertTrue(SensitiveFieldFilter.isSensitive("password"));
        assertTrue(SensitiveFieldFilter.isSensitive("newPassword"));
        assertTrue(SensitiveFieldFilter.isSensitive("confirmPassword"));
        assertTrue(SensitiveFieldFilter.isSensitive("verifyCode"));
        assertTrue(SensitiveFieldFilter.isSensitive("resetToken"));

        assertFalse(SensitiveFieldFilter.isSensitive("studentId"));
        assertFalse(SensitiveFieldFilter.isSensitive("email"));
        assertFalse(SensitiveFieldFilter.isSensitive("name"));
    }
}
