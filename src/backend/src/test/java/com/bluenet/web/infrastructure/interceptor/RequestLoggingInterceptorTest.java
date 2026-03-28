package com.bluenet.web.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * RequestLoggingInterceptor单元测试
 */
@ExtendWith(MockitoExtension.class)
class RequestLoggingInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    private RequestLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RequestLoggingInterceptor();
    }

    @Test
    void afterCompletion_shouldLogSuccessRequest() {
        // 模拟请求数据
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(200);

        // 执行方法
        interceptor.afterCompletion(request, response, handler, null);

        // 验证方法调用
        verify(request).getMethod();
        verify(request).getRequestURI();
        verify(request).getQueryString();
        verify(response).getStatus();
        // 注意：由于Logger是静态的，无法直接验证日志输出
        // 但可以验证方法执行没有抛出异常
    }

    @Test
    void afterCompletion_shouldLogRequestWithQueryParams() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getQueryString()).thenReturn("name=test&page=1");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, handler, null);

        verify(request).getMethod();
        verify(request).getRequestURI();
        verify(request).getQueryString();
        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldLogClientError() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/login");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(401);

        interceptor.afterCompletion(request, response, handler, null);

        verify(request).getMethod();
        verify(request).getRequestURI();
        verify(request).getQueryString();
        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldLogServerError() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/system");
        when(request.getQueryString()).thenReturn(null);
        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(request, response, handler, null);

        verify(request).getMethod();
        verify(request).getRequestURI();
        verify(request).getQueryString();
        verify(response).getStatus();
    }

    @Test
    void afterCompletion_shouldHandleEmptyQueryString() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getQueryString()).thenReturn("");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, handler, null);

        verify(request).getMethod();
        verify(request).getRequestURI();
        verify(request).getQueryString();
        verify(response).getStatus();
    }
}
