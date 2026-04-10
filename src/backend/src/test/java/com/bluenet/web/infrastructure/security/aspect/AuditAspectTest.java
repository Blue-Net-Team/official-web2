package com.bluenet.web.infrastructure.security.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.service.AuditService;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@DisplayName("AuditAspect 单元测试")
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private RequiresPermission requiresPermission;

    private AuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AuditAspect(auditService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        UserCTX.clear();
    }

    private void setUpRequest(String uri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setMethod(method);
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "TestAgent/1.0");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void setUpUser() {
        UserVO user = UserVO.builder()
                .id(42L)
                .studentId("2024001001")
                .username("testuser")
                .build();
        UserCTX.setCurrentUser(user);
    }

    @Test
    @DisplayName("成功的 GET 请求应正确记录审计日志")
    void successfulGetRequest_shouldRecordAudit() throws Throwable {
        setUpRequest("/api/v1/users", "GET");
        setUpUser();

        ResponseMessage<String> responseBody = ResponseMessage.success("data");
        ResponseEntity<ResponseMessage<String>> response = ResponseEntity.ok(responseBody);
        when(joinPoint.proceed()).thenReturn(response);

        Object result = aspect.audit(joinPoint, requiresPermission);

        assertEquals(response, result);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals("GET", audit.getRequestMethod());
        assertEquals("/api/v1/users", audit.getRequestUri());
        assertEquals("192.168.1.100", audit.getIpAddress());
        assertEquals("TestAgent/1.0", audit.getUserAgent());
        assertEquals(42L, audit.getActionUserId());
        assertEquals(200, audit.getHttpStatus());
        assertEquals("Success", audit.getResponseMessage());
        assertTrue(audit.getSuccessState());
        assertNotNull(audit.getDurationMs());
        assertNull(audit.getStackTrace());
    }

    @Test
    @DisplayName("权限异常应被记录为 403")
    void forbiddenException_shouldRecord403() throws Throwable {
        setUpRequest("/api/v1/admin/users", "DELETE");
        setUpUser();
        when(joinPoint.proceed()).thenThrow(new Forbidden("无权限"));

        assertThrows(Forbidden.class, () -> aspect.audit(joinPoint, requiresPermission));

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals(403, audit.getHttpStatus());
        assertEquals("无权限", audit.getResponseMessage());
        assertNotNull(audit.getStackTrace());
        assertTrue(audit.getStackTrace().contains("Forbidden"));
        assertTrue(audit.getSuccessState() == false);
    }

    @Test
    @DisplayName("未认证异常应被记录为 401")
    void unauthorizedException_shouldRecord401() throws Throwable {
        setUpRequest("/api/v1/protected", "GET");

        when(joinPoint.proceed()).thenThrow(new Unauthorized("未认证"));

        assertThrows(Unauthorized.class, () -> aspect.audit(joinPoint, requiresPermission));

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals(401, audit.getHttpStatus());
        assertEquals("未认证", audit.getResponseMessage());
        assertNull(audit.getActionUserId());
    }

    @Test
    @DisplayName("业务异常应被记录正确状态码")
    void globalException_shouldRecordCorrectStatus() throws Throwable {
        setUpRequest("/api/v1/test", "POST");
        setUpUser();

        when(joinPoint.proceed()).thenThrow(new GlobalException("服务器内部错误"));

        assertThrows(GlobalException.class, () -> aspect.audit(joinPoint, requiresPermission));

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals(500, audit.getHttpStatus());
        assertEquals("服务器内部错误", audit.getResponseMessage());
        assertNotNull(audit.getStackTrace());
    }

    @Test
    @DisplayName("未捕获异常应被记录为 500")
    void unknownException_shouldRecord500() throws Throwable {
        setUpRequest("/api/v1/test", "POST");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("unexpected"));

        assertThrows(RuntimeException.class, () -> aspect.audit(joinPoint, requiresPermission));

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals(500, audit.getHttpStatus());
        assertEquals("unexpected", audit.getResponseMessage());
    }

    @Test
    @DisplayName("没有 Request 上下文时应直接放行不记录审计")
    void noRequestContext_shouldProceedWithoutAudit() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.audit(joinPoint, requiresPermission);

        assertEquals("ok", result);
        verify(auditService, never()).save(any());
    }

    @Test
    @DisplayName("请求耗时应该被记录")
    void duration_shouldBeRecorded() throws Throwable {
        setUpRequest("/api/v1/slow", "GET");
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(10);
            return ResponseEntity.ok(ResponseMessage.success());
        });

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertNotNull(audit.getDurationMs());
        assertTrue(audit.getDurationMs() >= 10, "durationMs should be >= 10ms");
    }

    @Test
    @DisplayName("请求参数应被序列化到 actionArg")
    void parameters_shouldBeSerialized() throws Throwable {
        setUpRequest("/api/v1/login", "POST");
        when(joinPoint.getArgs()).thenReturn(new Object[] { "studentId123", "passwordValue" });
        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok(ResponseMessage.success()));

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertNotNull(audit.getActionArg());
        assertTrue(audit.getActionArg().contains("arg0"));
        assertTrue(audit.getActionArg().contains("arg1"));
    }

    @Test
    @DisplayName("异常堆栈超过 2000 字符应被截断")
    void longStackTrace_shouldBeTruncated() throws Throwable {
        setUpRequest("/api/v1/test", "GET");

        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            longMessage.append("x");
        }
        when(joinPoint.proceed()).thenThrow(new RuntimeException(longMessage.toString()));

        assertThrows(RuntimeException.class, () -> aspect.audit(joinPoint, requiresPermission));

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertNotNull(audit.getStackTrace());
        assertTrue(
                audit.getStackTrace().length() <= 2000,
                "Stack trace should be truncated to 2000 chars, was: " + audit.getStackTrace().length());
    }

    @Test
    @DisplayName("未认证用户的 actionUserId 应为 null")
    void unauthenticatedUser_actionUserIdShouldBeNull() throws Throwable {
        setUpRequest("/api/v1/public", "GET");

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok(ResponseMessage.success()));

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertNull(audit.getActionUserId());
    }

    // ==================== requestUriPattern ====================

    @Test
    @DisplayName("存在 BEST_MATCHING_PATTERN_ATTRIBUTE 时应记录 URI 模板")
    void withPatternAttribute_shouldRecordPattern() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/file/download/1001");
        request.setMethod("GET");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/file/download/{fileId}");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok(ResponseMessage.success()));

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals("/api/v1/file/download/{fileId}", audit.getRequestUriPattern());
        assertEquals("/api/v1/file/download/1001", audit.getRequestUri());
    }

    @Test
    @DisplayName("BEST_MATCHING_PATTERN_ATTRIBUTE 为 null 时应回退到原始 requestUri")
    void withoutPatternAttribute_shouldFallbackToRequestUri() throws Throwable {
        setUpRequest("/api/v1/test", "GET");

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok(ResponseMessage.success()));

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals("/api/v1/test", audit.getRequestUriPattern());
        assertEquals("/api/v1/test", audit.getRequestUri());
    }

    @Test
    @DisplayName("多路径参数的 URI 模板应被正确记录")
    void multiPathVariable_shouldRecordFullPattern() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin/competitions/5/images/10");
        request.setMethod("DELETE");
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
                "/api/v1/admin/competitions/{id}/images/{imageId}");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok(ResponseMessage.success()));

        aspect.audit(joinPoint, requiresPermission);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(auditService).save(captor.capture());

        Audit audit = captor.getValue();
        assertEquals("/api/v1/admin/competitions/{id}/images/{imageId}", audit.getRequestUriPattern());
        assertEquals("/api/v1/admin/competitions/5/images/10", audit.getRequestUri());
    }
}
