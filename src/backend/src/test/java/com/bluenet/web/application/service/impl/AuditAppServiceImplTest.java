package com.bluenet.web.application.service.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.bluenet.web.application.command.audit.AuditCommands;
import com.bluenet.web.domain.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AuditAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AuditAppServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditAppServiceImpl auditAppService;

    @BeforeEach
    void setUp() {
        auditAppService = new AuditAppServiceImpl(auditRepository);
    }

    @Test
    @DisplayName("saveAudit 应调用 repository.save")
    void saveAudit_shouldCallRepositorySave() {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "GET", "/api/v1/test", "/api/v1/test",
                null, null, null,
                "127.0.0.1", "TestAgent",
                200, "Success", null,
                100L, true);

        auditAppService.saveAudit(command);

        verify(auditRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("repository 异常时不应抛出（吞掉异常避免影响业务）")
    void saveAudit_whenRepositoryFails_shouldNotThrow() {
        AuditCommands.SaveAuditCommand command = new AuditCommands.SaveAuditCommand(
                "GET", "/api/v1/test", "/api/v1/test",
                null, null, null,
                "127.0.0.1", "TestAgent",
                200, "Success", null,
                100L, true);
        doThrow(new RuntimeException("DB error")).when(auditRepository).save(org.mockito.ArgumentMatchers.any());

        // Should not throw
        auditAppService.saveAudit(command);

        verify(auditRepository).save(org.mockito.ArgumentMatchers.any());
    }
}
