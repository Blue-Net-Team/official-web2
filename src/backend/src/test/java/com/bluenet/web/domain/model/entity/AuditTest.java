package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Audit 领域实体单元测试。
 */
@DisplayName("Audit 领域实体测试")
class AuditTest {

    @Test
    @DisplayName("create: 应创建新的审计日志")
    void create_shouldCreateAudit() {
        LocalDateTime before = LocalDateTime.now();
        Audit audit = Audit.create(
                "POST",
                "/api/users",
                "/api/users",
                "{\"name\":\"test\"}",
                1L,
                "127.0.0.1",
                "Mozilla/5.0");
        LocalDateTime after = LocalDateTime.now();

        assertThat(audit.getId()).isNull();
        assertThat(audit.getRequestMethod()).isEqualTo("POST");
        assertThat(audit.getRequestUri()).isEqualTo("/api/users");
        assertThat(audit.getRequestUriPattern()).isEqualTo("/api/users");
        assertThat(audit.getActionArg()).isEqualTo("{\"name\":\"test\"}");
        assertThat(audit.getActionUserId()).isEqualTo(1L);
        assertThat(audit.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(audit.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(audit.getActionTime()).isAfterOrEqualTo(before);
        assertThat(audit.getActionTime()).isBeforeOrEqualTo(after);
        assertThat(audit.getHttpStatus()).isNull();
        assertThat(audit.getResponseMessage()).isNull();
        assertThat(audit.getStackTrace()).isNull();
        assertThat(audit.getDurationMs()).isNull();
        assertThat(audit.getSuccessState()).isNull();
    }

    @Test
    @DisplayName("recordSuccess: 应记录成功状态")
    void recordSuccess_shouldRecordSuccessState() {
        Audit audit = Audit.create("GET", "/api/test", "/api/test", null, null, null, null);

        audit.recordSuccess(200, "OK");

        assertThat(audit.getHttpStatus()).isEqualTo(200);
        assertThat(audit.getResponseMessage()).isEqualTo("OK");
        assertThat(audit.getSuccessState()).isTrue();
        assertThat(audit.getStackTrace()).isNull();
    }

    @Test
    @DisplayName("recordFailure: 应记录失败状态及堆栈")
    void recordFailure_shouldRecordFailureState() {
        Audit audit = Audit.create("GET", "/api/test", "/api/test", null, null, null, null);

        audit.recordFailure(500, "Internal Server Error", "stack trace here");

        assertThat(audit.getHttpStatus()).isEqualTo(500);
        assertThat(audit.getResponseMessage()).isEqualTo("Internal Server Error");
        assertThat(audit.getStackTrace()).isEqualTo("stack trace here");
        assertThat(audit.getSuccessState()).isFalse();
    }

    @Test
    @DisplayName("setDuration: 应设置请求耗时")
    void setDuration_shouldSetDurationMs() {
        Audit audit = Audit.create("GET", "/api/test", "/api/test", null, null, null, null);

        audit.setDuration(150L);

        assertThat(audit.getDurationMs()).isEqualTo(150L);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime actionTime = LocalDateTime.of(2024, 3, 15, 9, 0);
        Audit audit = Audit.reconstruct(
                100L,
                "PUT",
                "/api/users/1",
                "/api/users/{id}",
                "arg",
                2L,
                actionTime,
                "192.168.1.1",
                "Chrome",
                200,
                "OK",
                null,
                80L,
                true);

        assertThat(audit.getId()).isEqualTo(100L);
        assertThat(audit.getRequestMethod()).isEqualTo("PUT");
        assertThat(audit.getRequestUri()).isEqualTo("/api/users/1");
        assertThat(audit.getRequestUriPattern()).isEqualTo("/api/users/{id}");
        assertThat(audit.getActionArg()).isEqualTo("arg");
        assertThat(audit.getActionUserId()).isEqualTo(2L);
        assertThat(audit.getActionTime()).isEqualTo(actionTime);
        assertThat(audit.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(audit.getUserAgent()).isEqualTo("Chrome");
        assertThat(audit.getHttpStatus()).isEqualTo(200);
        assertThat(audit.getResponseMessage()).isEqualTo("OK");
        assertThat(audit.getStackTrace()).isNull();
        assertThat(audit.getDurationMs()).isEqualTo(80L);
        assertThat(audit.getSuccessState()).isTrue();
    }
}
