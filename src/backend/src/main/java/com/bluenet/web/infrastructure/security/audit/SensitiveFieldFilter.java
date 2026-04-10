package com.bluenet.web.infrastructure.security.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 敏感字段脱敏工具
 * <p>
 * 在序列化请求参数时将敏感字段值替换为 "***"
 * </p>
 */
public class SensitiveFieldFilter {

    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "newPassword",
            "confirmPassword",
            "verifyCode",
            "resetToken");

    /**
     * 对 Map 中的敏感字段值进行脱敏
     */
    @SuppressWarnings("unchecked")
    public static String maskSensitiveFields(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        Map<String, Object> masked = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (SENSITIVE_FIELDS.contains(entry.getKey())) {
                masked.put(entry.getKey(), MASK);
            } else if (entry.getValue() instanceof Map) {
                masked.put(entry.getKey(), maskMap((Map<String, Object>) entry.getValue()));
            } else {
                masked.put(entry.getKey(), entry.getValue());
            }
        }

        return serializeMap(masked);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> maskMap(Map<String, Object> map) {
        Map<String, Object> masked = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (SENSITIVE_FIELDS.contains(entry.getKey())) {
                masked.put(entry.getKey(), MASK);
            } else if (entry.getValue() instanceof Map) {
                masked.put(entry.getKey(), maskMap((Map<String, Object>) entry.getValue()));
            } else {
                masked.put(entry.getKey(), entry.getValue());
            }
        }
        return masked;
    }

    private static String serializeMap(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first)
                sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 判断字段名是否为敏感字段
     */
    public static boolean isSensitive(String fieldName) {
        return SENSITIVE_FIELDS.contains(fieldName);
    }

    public static Set<String> getSensitiveFields() {
        return SENSITIVE_FIELDS;
    }
}
