package com.bluenet.web.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志记录拦截器
 * <p>
 * 在控制层处理完成后记录HTTP请求的基本信息，格式为： {方法} {URI} {状态码} {状态文本}，例如：GET /api/v1/users 200
 * OK
 * </p>
 */
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUri = queryString != null && !queryString.isEmpty() ? uri + "?" + queryString : uri;
        int status = response.getStatus();
        String statusText = getStatusText(status);

        String logMessage = String.format("%s %s %d %s", method, fullUri, status, statusText);

        // 根据状态码设置不同的日志级别
        if (status >= 200 && status < 300) {
            logger.info(logMessage);
        } else if (status >= 400 && status < 500) {
            logger.warn(logMessage);
        } else if (status >= 500) {
            logger.error(logMessage);
        } else {
            // 其他状态码（1xx, 3xx）使用INFO级别
            logger.info(logMessage);
        }
    }

    /**
     * 获取HTTP状态码对应的状态文本
     */
    private String getStatusText(int status) {
        // 找出对应的http status枚举
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        return httpStatus.getReasonPhrase();
    }
}
