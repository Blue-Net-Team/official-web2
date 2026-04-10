package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Audit;

/**
 * 审计日志仓库接口
 */
public interface AuditRepository {
    void insert(Audit audit);
}
