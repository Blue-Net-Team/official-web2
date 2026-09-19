package com.bluenet.web.infrastructure.security.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * AuditParameterSerializer 单元测试。
 * <p>
 * 使用与 WebMvcConfig 中 Spring 容器 ObjectMapper 相同的配置（注册 JavaTimeModule），
 * 覆盖审计参数序列化与脱敏。
 * </p>
 */
@DisplayName("AuditParameterSerializer 单元测试")
class AuditParameterSerializerTest {

    private ObjectMapper objectMapper;
    private AuditParameterSerializer serializer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serializer = new AuditParameterSerializer(objectMapper);
    }

    @Test
    @DisplayName("含 LocalDate 参数的请求应正常序列化，不得退化为 serialization failed")
    void localDateParameter_shouldBeSerialized() throws Exception {
        AchievementRequest request = new AchievementRequest("蓝桥杯全国一等奖", LocalDate.of(2024, 4, 15));

        String result = serializer.serialize(new String[] { "request" }, new Object[] { request });

        assertFalse(result.contains("serialization failed"), "应正常序列化，实际: " + result);
        JsonNode node = objectMapper.readTree(result).get("request");
        assertEquals("2024-04-15", node.get("achieveAt").asText());
        assertEquals("蓝桥杯全国一等奖", node.get("title").asText());
    }

    @Test
    @DisplayName("含 LocalDateTime 参数的请求应正常序列化")
    void localDateTimeParameter_shouldBeSerialized() throws Exception {
        String result = serializer.serialize(
                new String[] { "startTime" },
                new Object[] { LocalDateTime.of(2026, 9, 19, 17, 29, 19) });

        assertFalse(result.contains("serialization failed"), "应正常序列化，实际: " + result);
        assertEquals("2026-09-19T17:29:19", objectMapper.readTree(result).get("startTime").asText());
    }

    @Test
    @DisplayName("嵌套请求对象应输出嵌套 JSON 且敏感字段被脱敏")
    void nestedRequest_shouldBeNestedJsonWithMaskedFields() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("verify-token", "newHash", "newHash");

        String result = serializer.serialize(
                new String[] { "request", "userId" },
                new Object[] { request, 42L });

        JsonNode node = objectMapper.readTree(result);
        assertTrue(node.get("request").isObject(), "request 应为 JSON 对象，实际: " + result);
        assertEquals("***", node.get("request").get("token").asText());
        assertEquals("***", node.get("request").get("newPassword").asText());
        assertEquals("***", node.get("request").get("confirmPassword").asText());
        assertEquals(42L, node.get("userId").asLong());
        assertFalse(result.contains("verify-token"));
        assertFalse(result.contains("newHash"));
    }

    @Test
    @DisplayName("数组参数元素中的敏感字段应被脱敏")
    void listParameter_shouldMaskElementFields() throws Exception {
        List<AchievementRequest> requests = List.of(
                new AchievementRequest("A", LocalDate.of(2024, 1, 1)),
                new AchievementRequest("B", LocalDate.of(2024, 2, 2)));

        String result = serializer.serialize(new String[] { "requests" }, new Object[] { requests });

        JsonNode items = objectMapper.readTree(result).get("requests");
        assertTrue(items.isArray());
        assertEquals("2024-01-01", items.get(0).get("achieveAt").asText());
        assertEquals("2024-02-02", items.get(1).get("achieveAt").asText());
    }

    @Test
    @DisplayName("Servlet 对象、文件与 null 参数应被跳过")
    void excludedParameters_shouldBeSkipped() {
        HttpServletRequest servletRequest = new MockHttpServletRequest();
        MultipartFile multipartFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "a.txt", "text/plain", "content".getBytes());

        String result = serializer.serialize(
                new String[] { "request", "file", "session", "nullArg" },
                new Object[] { null, multipartFile, servletRequest, null });

        assertNull(result, "全部参数都被排除时应返回 null");
    }

    @Test
    @DisplayName("仅输出流参数时应返回 null")
    void outputStreamOnly_shouldReturnNull() {
        assertNull(serializer.serialize(new String[] { "out" }, new Object[] { OutputStream.nullOutputStream() }));
    }

    @Test
    @DisplayName("无参数时应返回 null")
    void emptyArguments_shouldReturnNull() {
        assertNull(serializer.serialize(new String[] {}, new Object[] {}));
        assertNull(serializer.serialize(new String[] {}, null));
    }

    @Test
    @DisplayName("参数名缺失时应使用 arg0、arg1 占位")
    void missingParameterNames_shouldFallbackToArgIndex() throws Exception {
        String result = serializer.serialize(null, new Object[] { 1L, "text" });

        JsonNode node = objectMapper.readTree(result);
        assertEquals(1L, node.get("arg0").asLong());
        assertEquals("text", node.get("arg1").asText());
    }

    @Test
    @DisplayName("序列化异常时应返回错误占位 JSON 且不抛出异常")
    void serializationFailure_shouldReturnPlaceholder() throws Exception {
        String result = serializer.serialize(new String[] { "boom" }, new Object[] { new BrokenValue() });

        assertTrue(result.contains("serialization failed"), "实际: " + result);
        assertTrue(objectMapper.readTree(result).isObject(), "占位内容应为合法 JSON");
    }

    /** 模拟 CreateAchievementRequestDTO 的日期字段 */
    private record AchievementRequest(String title, LocalDate achieveAt) {
    }

    /** 模拟 ChangePasswordRequestDTO 的敏感字段 */
    private record ChangePasswordRequest(String token, String newPassword, String confirmPassword) {
    }

    private static class BrokenValue {
        @SuppressWarnings("unused")
        public String getBoom() {
            throw new IllegalStateException("boom");
        }
    }
}
