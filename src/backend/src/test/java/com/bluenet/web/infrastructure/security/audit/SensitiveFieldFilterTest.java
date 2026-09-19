package com.bluenet.web.infrastructure.security.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SensitiveFieldFilter 单元测试")
class SensitiveFieldFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("password 字段应被脱敏为 ***")
    void password_shouldBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("studentId", "2024001001");
        params.put("password", "secretHash123");

        String result = mask(params);

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
        params.put("token", "verify-token-abc");

        String result = mask(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertFalse(result.contains("newPass123"));
        assertFalse(result.contains("token-abc"));
        assertFalse(result.contains("verify-token-abc"));
    }

    @Test
    @DisplayName("verifyCode 字段应被脱敏")
    void verifyCode_shouldBeMasked() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("email", "test@test.com");
        params.put("verifyCode", "123456");

        String result = mask(params);

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

        String result = mask(params);

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

        String result = mask(params);

        assertNotNull(result);
        assertTrue(result.contains("***"));
        assertFalse(result.contains("innerPass"));
        assertTrue(result.contains("123"));
    }

    @Test
    @DisplayName("嵌套对象应序列化为嵌套 JSON 而非 Java toString")
    void nestedObject_shouldBeNestedJson() throws Exception {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("title", "Linux开发板的使用");
        inner.put("relatedLink", null);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("slug", "cv");
        params.put("request", inner);

        String result = mask(params);

        JsonNode node = objectMapper.readTree(result);
        assertTrue(node.get("request").isObject(), "request 应为 JSON 对象，实际: " + result);
        assertTrue(node.get("request").get("relatedLink").isNull(), "null 应保持 JSON null");
        assertTrue(node.get("request").get("title").isTextual());
        assertEquals("Linux开发板的使用", node.get("request").get("title").asText());
    }

    @Test
    @DisplayName("数组元素中的敏感字段也应被脱敏且保持 JSON 数组结构")
    void arrayElements_shouldBeMasked() throws Exception {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("id", 1);
        first.put("password", "leakedPass");
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("id", 2);
        second.put("password", "anotherLeak");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("items", List.of(first, second));

        String result = mask(params);

        JsonNode items = objectMapper.readTree(result).get("items");
        assertTrue(items.isArray(), "items 应为 JSON 数组，实际: " + result);
        assertTrue(items.get(0).isObject(), "数组元素应为 JSON 对象，实际: " + result);
        assertEquals("***", items.get(0).get("password").asText());
        assertEquals("***", items.get(1).get("password").asText());
        assertFalse(result.contains("leakedPass"));
        assertFalse(result.contains("anotherLeak"));
    }

    @Test
    @DisplayName("自定义类型字段应保留其字段结构并被完整脱敏")
    void pojoField_shouldKeepStructure() throws Exception {
        JsonNode node = objectMapper.valueToTree(Map.of("request", new SampleRequest("标题", "明文密码")));

        SensitiveFieldFilter.maskSensitiveFields(node);

        JsonNode request = node.get("request");
        assertTrue(request.isObject());
        assertEquals("标题", request.get("title").asText());
        assertEquals("***", request.get("password").asText());
    }

    @Test
    @DisplayName("null 或空 Map 应返回 null")
    void nullOrEmpty_shouldReturnNull() {
        assertNull(SensitiveFieldFilter.maskSensitiveFields((JsonNode) null));
        assertNull(mask(new LinkedHashMap<>()));
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
        assertFalse(SensitiveFieldFilter.isSensitive(null));
    }

    /** 与切面一致：先转成 JSON 节点树，脱敏后再写回 JSON 字符串 */
    private String mask(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        JsonNode node = objectMapper.valueToTree(params);
        SensitiveFieldFilter.maskSensitiveFields(node);
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private record SampleRequest(String title, String password) {
    }

    @Test
    @DisplayName("标量节点不应被修改")
    void scalarNode_shouldNotBeModified() {
        JsonNode textNode = JsonNodeFactory.instance.textNode("plain");
        assertEquals("plain", SensitiveFieldFilter.maskSensitiveFields(textNode).asText());
    }
}
