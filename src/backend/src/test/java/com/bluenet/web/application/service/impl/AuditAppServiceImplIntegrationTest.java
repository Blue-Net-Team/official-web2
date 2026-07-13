package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.audit.AuditCommands;
import com.bluenet.web.application.service.AuditAppService;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditAppServiceImpl 集成测试。
 *
 * <p>
 * 验证审计日志应用服务通过异步执行器持久化审计日志的行为。
 * </p>
 */
@DisplayName("AuditAppServiceImpl 集成测试")
class AuditAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuditAppService auditAppService;

    @Autowired
    private AuditMapper auditMapper;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("saveAudit: 应异步持久化基础审计日志字段")
    void saveAudit_shouldPersistBasicAuditLogFields() throws InterruptedException {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "GET",
                "/api/v1/test",
                "/api/v1/test",
                "{}",
                1L,
                LocalDateTime.now(),
                "127.0.0.1",
                "UnitTest",
                null,
                null,
                null,
                null,
                null);

        auditAppService.saveAudit(command);
        Thread.sleep(500L);

        assertThat(auditMapper.selectCount(null)).isEqualTo(1L);
        List<AuditDO> audits = auditMapper.selectList(null);
        assertThat(audits).hasSize(1);
        AuditDO saved = audits.get(0);
        assertThat(saved.getRequestMethod()).isEqualTo("GET");
        assertThat(saved.getRequestUri()).isEqualTo("/api/v1/test");
        assertThat(saved.getRequestUriPattern()).isEqualTo("/api/v1/test");
        assertThat(saved.getActionArg()).isEqualTo("{}");
        assertThat(saved.getActionUserId()).isEqualTo(1L);
        assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("UnitTest");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("saveAudit: 应异步持久化成功审计日志")
    void saveAudit_shouldPersistSuccessfulAuditLog() throws InterruptedException {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "POST",
                "/api/v1/success",
                "/api/v1/success",
                null,
                2L,
                LocalDateTime.now(),
                "127.0.0.1",
                "UnitTest",
                200,
                "OK",
                null,
                null,
                true);

        auditAppService.saveAudit(command);
        Thread.sleep(500L);

        assertThat(auditMapper.selectCount(null)).isEqualTo(1L);
        AuditDO saved = auditMapper.selectList(null).get(0);
        assertThat(saved.getHttpStatus()).isEqualTo(200);
        assertThat(saved.getResponseMessage()).isEqualTo("OK");
        assertThat(saved.getSuccessState()).isTrue();
        assertThat(saved.getStackTrace()).isNull();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("saveAudit: 应异步持久化失败审计日志")
    void saveAudit_shouldPersistFailedAuditLog() throws InterruptedException {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "POST",
                "/api/v1/failure",
                "/api/v1/failure",
                null,
                3L,
                LocalDateTime.now(),
                "127.0.0.1",
                "UnitTest",
                500,
                "Internal Server Error",
                "java.lang.RuntimeException: test failure",
                null,
                false);

        auditAppService.saveAudit(command);
        Thread.sleep(500L);

        assertThat(auditMapper.selectCount(null)).isEqualTo(1L);
        AuditDO saved = auditMapper.selectList(null).get(0);
        assertThat(saved.getHttpStatus()).isEqualTo(500);
        assertThat(saved.getResponseMessage()).isEqualTo("Internal Server Error");
        assertThat(saved.getStackTrace()).isEqualTo("java.lang.RuntimeException: test failure");
        assertThat(saved.getSuccessState()).isFalse();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("saveAudit: 应异步持久化包含耗时的审计日志")
    void saveAudit_shouldPersistAuditLogWithDuration() throws InterruptedException {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "GET",
                "/api/v1/duration",
                "/api/v1/duration",
                null,
                4L,
                LocalDateTime.now(),
                "127.0.0.1",
                "UnitTest",
                null,
                null,
                null,
                150L,
                null);

        auditAppService.saveAudit(command);
        Thread.sleep(500L);

        assertThat(auditMapper.selectCount(null)).isEqualTo(1L);
        AuditDO saved = auditMapper.selectList(null).get(0);
        assertThat(saved.getDurationMs()).isEqualTo(150L);
    }
}
