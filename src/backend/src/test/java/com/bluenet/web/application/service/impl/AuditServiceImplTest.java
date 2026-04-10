package com.bluenet.web.application.service.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("AuditServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditRepository);
    }

    @Test
    @DisplayName("save 应调用 repository.insert")
    void save_shouldCallRepositoryInsert() {
        Audit audit = new Audit();
        audit.setRequestMethod("GET");
        audit.setRequestUri("/api/v1/test");

        auditService.save(audit);

        verify(auditRepository).insert(audit);
    }

    @Test
    @DisplayName("repository 异常时不应抛出（吞掉异常避免影响业务）")
    void save_whenRepositoryFails_shouldNotThrow() {
        Audit audit = new Audit();
        doThrow(new RuntimeException("DB error")).when(auditRepository).insert(audit);

        // Should not throw
        auditService.save(audit);

        verify(auditRepository).insert(audit);
    }
}
