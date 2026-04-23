package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.audit.AuditCommands;
import com.bluenet.web.application.service.AuditAppService;
import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志应用服务实现。
 * <p>
 * 实现审计日志聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditAppServiceImpl implements AuditAppService {

    private final AuditRepository auditRepository;

    /**
     * 保存审计日志。
     *
     * @param command
     *            保存审计日志命令
     */
    @Override
    @Async("auditExecutor")
    public void saveAudit(AuditCommands.SaveAuditCommand command) {
        try {
            Audit audit = Audit.create(
                    command.requestMethod(),
                    command.requestUri(),
                    command.requestUriPattern(),
                    command.actionArg(),
                    command.actionUserId(),
                    command.ipAddress(),
                    command.userAgent());
            if (command.httpStatus() != null) {
                if (Boolean.TRUE.equals(command.successState())) {
                    audit.recordSuccess(command.httpStatus(), command.responseMessage());
                } else {
                    audit.recordFailure(command.httpStatus(), command.responseMessage(), command.stackTrace());
                }
            }
            if (command.durationMs() != null) {
                audit.setDuration(command.durationMs());
            }
            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("审计日志写入失败: {}", e.getMessage(), e);
        }
    }
}
