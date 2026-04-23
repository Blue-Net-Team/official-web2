package com.bluenet.web.application.service;

import com.bluenet.web.application.command.audit.AuditCommands;

/**
 * 审计日志应用服务接口。
 * <p>
 * 定义了审计日志聚合在应用层的所有业务操作。
 * </p>
 */
public interface AuditAppService {

    /**
     * 保存审计日志
     *
     * @param command
     *            保存审计日志命令
     */
    void saveAudit(AuditCommands.SaveAuditCommand command);
}
