package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.service.AuditService;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    @Override
    @Async("auditExecutor")
    public void save(Audit audit) {
        try {
            auditRepository.insert(audit);
        } catch (Exception e) {
            log.error("审计日志写入失败: {}", e.getMessage(), e);
        }
    }
}
