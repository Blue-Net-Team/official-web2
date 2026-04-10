package com.bluenet.web.infrastructure.security.aspect;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.service.AuditService;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.infrastructure.security.audit.SensitiveFieldFilter;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.IpUtils;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 审计切面，拦截所有带 @RequiresPermission 注解的方法，自动记录请求审计日志。
 * <p>
 * 执行顺序：AuditAspect (@Order(1)) 最外层，可捕获 PermissionAspect 和 RateLimitAspect
 * 抛出的异常。
 * </p>
 */
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    private static final int MAX_STACK_TRACE_LENGTH = 2000;

    private static final Set<Class<?>> EXCLUDED_PARAM_TYPES = new HashSet<>(Arrays.asList(
            HttpServletRequest.class,
            HttpServletResponse.class,
            InputStream.class,
            OutputStream.class,
            Principal.class,
            Locale.class));

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    @Around("@annotation(requiresPermission)")
    public Object audit(ProceedingJoinPoint pjp, RequiresPermission requiresPermission) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return pjp.proceed();
        }

        Audit audit = new Audit();
        audit.setRequestMethod(request.getMethod());
        audit.setRequestUri(request.getRequestURI());
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        audit.setRequestUriPattern(pattern != null ? pattern : request.getRequestURI());
        audit.setIpAddress(IpUtils.getClientIp(request));
        audit.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
        audit.setActionUserId(UserCTX.getCurrentUserId());
        audit.setActionTime(LocalDateTime.now());
        audit.setActionArg(serializeParameters(pjp));

        try {
            Object result = pjp.proceed();
            extractResponseInfo(result, audit);
            audit.setSuccessState(true);
            return result;
        } catch (GlobalException ex) {
            audit.setHttpStatus(ex.getCode().value());
            audit.setResponseMessage(ex.getMessage());
            audit.setStackTrace(truncateStackTrace(ex));
            audit.setSuccessState(false);
            throw ex;
        } catch (Throwable ex) {
            audit.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            audit.setResponseMessage(ex.getMessage());
            audit.setStackTrace(truncateStackTrace(ex));
            audit.setSuccessState(false);
            throw ex;
        } finally {
            audit.setDurationMs(System.currentTimeMillis() - startTime);
            auditService.save(audit);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 从成功响应中提取 HTTP 状态码和响应消息
     */
    private void extractResponseInfo(Object result, Audit audit) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            audit.setHttpStatus(responseEntity.getStatusCode().value());
            Object body = responseEntity.getBody();
            if (body instanceof ResponseMessage<?> rm) {
                audit.setResponseMessage(rm.getMsg());
            }
        } else if (result instanceof ResponseMessage<?> rm) {
            audit.setHttpStatus(rm.getCode());
            audit.setResponseMessage(rm.getMsg());
        } else {
            audit.setHttpStatus(HttpStatus.OK.value());
        }
    }

    /**
     * 序列化方法参数为 JSON 字符串，跳过不可序列化的参数类型，并对敏感字段脱敏 使用方法签名中的真实参数名替代 arg0, arg1 等
     */
    @SuppressWarnings("unchecked")
    private String serializeParameters(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        String[] paramNames = null;
        if (pjp.getSignature()instanceof MethodSignature ms) {
            paramNames = ms.getParameterNames();
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
            // 先序列化为 Map 结构，再对敏感字段脱敏
            String json = objectMapper.writeValueAsString(params);
            Map<String, Object> map = objectMapper.readValue(json, LinkedHashMap.class);
            return SensitiveFieldFilter.maskSensitiveFields(map);
        } catch (Exception e) {
            log.warn("审计参数序列化失败: {}", e.getMessage());
            return "{\"error\":\"serialization failed\"}";
        }
    }

    /**
     * 截断异常堆栈到最大长度
     */
    private String truncateStackTrace(Throwable ex) {
        if (ex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName());
        if (ex.getMessage() != null) {
            sb.append(": ").append(ex.getMessage());
        }
        sb.append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return truncate(sb.toString(), MAX_STACK_TRACE_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
