package com.bluenet.web.infrastructure.security.aspect;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.service.AuditAppService;
import com.bluenet.web.application.command.audit.AuditCommands;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.infrastructure.security.audit.AuditParameterSerializer;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.IpUtils;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;

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
public class AuditAspect {

    private final AuditAppService auditAppService;

    private final AuditParameterSerializer auditParameterSerializer;

    private static final int MAX_STACK_TRACE_LENGTH = 2000;

    @Around("@annotation(requiresPermission)")
    public Object audit(ProceedingJoinPoint pjp, RequiresPermission requiresPermission) throws Throwable {
        if (!requiresPermission.audit()) {
            return pjp.proceed();
        }

        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return pjp.proceed();
        }

        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestUriPattern = pattern != null ? pattern : request.getRequestURI();
        String ipAddress = IpUtils.getClientIp(request);
        String userAgent = truncate(request.getHeader("User-Agent"), 500);
        Long actionUserId = UserCTX.getCurrentUserId();
        String actionArg = serializeParameters(pjp);

        Integer httpStatus = null;
        String responseMessage = null;
        String stackTrace = null;
        Boolean successState = null;

        try {
            Object result = pjp.proceed();
            httpStatus = extractHttpStatus(result);
            responseMessage = extractResponseMessage(result);
            successState = true;
            return result;
        } catch (GlobalException ex) {
            httpStatus = ex.getCode().value();
            responseMessage = ex.getMessage();
            stackTrace = truncateStackTrace(ex);
            successState = false;
            throw ex;
        } catch (Throwable ex) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            responseMessage = ex.getMessage();
            stackTrace = truncateStackTrace(ex);
            successState = false;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                    requestMethod,
                    requestUri,
                    requestUriPattern,
                    actionArg,
                    actionUserId,
                    LocalDateTime.now(),
                    ipAddress,
                    userAgent,
                    httpStatus,
                    responseMessage,
                    stackTrace,
                    durationMs,
                    successState);
            auditAppService.saveAudit(command);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 从成功响应中提取 HTTP 状态码
     */
    private Integer extractHttpStatus(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        } else if (result instanceof ResponseMessage<?> rm) {
            return rm.getCode();
        } else {
            return HttpStatus.OK.value();
        }
    }

    /**
     * 从成功响应中提取响应消息
     */
    private String extractResponseMessage(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            if (body instanceof ResponseMessage<?> rm) {
                return rm.getMsg();
            }
        } else if (result instanceof ResponseMessage<?> rm) {
            return rm.getMsg();
        }
        return null;
    }

    /**
     * 序列化方法参数为 JSON 字符串，跳过不可序列化的参数类型，并对敏感字段脱敏。 使用方法签名中的真实参数名替代 arg0, arg1 等。
     */
    private String serializeParameters(ProceedingJoinPoint pjp) {
        String[] paramNames = null;
        if (pjp.getSignature()instanceof MethodSignature ms) {
            paramNames = ms.getParameterNames();
        }
        return auditParameterSerializer.serialize(paramNames, pjp.getArgs());
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
