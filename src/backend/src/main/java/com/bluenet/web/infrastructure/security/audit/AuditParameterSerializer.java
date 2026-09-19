package com.bluenet.web.infrastructure.security.audit;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 审计请求参数序列化器。
 * <p>
 * 使用 Spring 容器统一配置的 {@link ObjectMapper}（已注册 JavaTimeModule，支持 LocalDate /
 * LocalDateTime 等 java.time 类型），将请求参数序列化为 JSON 并对 敏感字段脱敏。
 * </p>
 * <p>
 * 不得使用 {@code new ObjectMapper()}：默认实例无法处理 java.time 类型， 会导致含日期参数的所有接口审计记录退化为
 * {@code {"error":"serialization failed"}}。
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditParameterSerializer {

    /** 不可序列化或不应记录内容的参数类型（Servlet 对象、IO 流、文件内容等） */
    private static final Set<Class<?>> EXCLUDED_PARAM_TYPES = Set.of(
            HttpServletRequest.class,
            HttpServletResponse.class,
            HttpSession.class,
            InputStream.class,
            OutputStream.class,
            MultipartFile.class,
            Principal.class,
            Locale.class);

    private final ObjectMapper objectMapper;

    /**
     * 将方法参数序列化为脱敏后的 JSON 字符串。
     *
     * @param paramNames
     *            方法签名中的参数名，可为 null（此时使用 arg0、arg1 占位）
     * @param args
     *            方法参数值
     * @return JSON 字符串；无需记录的参数时返回 null；序列化失败时返回错误占位 JSON
     */
    public String serialize(String[] paramNames, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null || EXCLUDED_PARAM_TYPES.stream().anyMatch(type -> type.isInstance(arg))) {
                continue;
            }
            String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
            params.put(name, arg);
        }

        if (params.isEmpty()) {
            return null;
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);
            SensitiveFieldFilter.maskSensitiveFields(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            log.warn("审计参数序列化失败: {}", e.getMessage());
            return "{\"error\":\"serialization failed\"}";
        }
    }
}
