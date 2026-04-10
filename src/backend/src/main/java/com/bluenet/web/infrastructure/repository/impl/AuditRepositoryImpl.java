package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.repository.AuditRepository;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {
    private final AuditMapper auditMapper;

    @Override
    public void insert(Audit audit) {
        auditMapper.insert(audit);
    }
}
