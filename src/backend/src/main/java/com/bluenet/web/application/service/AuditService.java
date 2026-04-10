package com.bluenet.web.application.service;

import com.bluenet.web.domain.model.entity.Audit;

/**
 * 审计日志服务接口
 */
public interface AuditService {
    void save(Audit audit);
}
