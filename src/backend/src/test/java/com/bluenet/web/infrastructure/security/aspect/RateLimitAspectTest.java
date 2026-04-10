package com.bluenet.web.infrastructure.security.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import com.bluenet.web.domain.exception.TooManyRequests;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@DisplayName("RateLimitAspect 单元测试")
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private RateLimit rateLimit;

    private RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RateLimitAspect(redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void setUpRequest(String uri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        setMethod(request, method);
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setMethod(MockHttpServletRequest request, String method) {
        request.setMethod(method);
    }

    @Test
    @DisplayName("首次请求应该放行")
    void firstRequest_shouldProceed() throws Throwable {
        setUpRequest("/api/v1/test", "POST");
        when(rateLimit.interval()).thenReturn(60);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, rateLimit);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("间隔内重复请求应该拒绝")
    void repeatedRequest_shouldReject() throws Throwable {
        setUpRequest("/api/v1/test", "POST");
        when(rateLimit.interval()).thenReturn(60);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThrows(TooManyRequests.class, () -> aspect.around(joinPoint, rateLimit));
    }

    @Test
    @DisplayName("不同接口独立计算限频")
    void differentEndpoints_shouldBeIndependent() throws Throwable {
        // 接口A被限频
        setUpRequest("/api/v1/a", "POST");
        when(rateLimit.interval()).thenReturn(60);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThrows(TooManyRequests.class, () -> aspect.around(joinPoint, rateLimit));

        // 接口B应放行（不同的 Redis Key）
        setUpRequest("/api/v1/b", "POST");
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, rateLimit);
        assertEquals("ok", result);
    }

    @Test
    @DisplayName("Redis 异常时降级放行")
    void redisException_shouldFallback() throws Throwable {
        setUpRequest("/api/v1/test", "POST");
        when(rateLimit.interval()).thenReturn(60);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RuntimeException("Redis connection failed"));
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, rateLimit);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("没有 Request 上下文时直接放行")
    void noRequestContext_shouldProceed() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, rateLimit);

        assertEquals("ok", result);
    }
}
