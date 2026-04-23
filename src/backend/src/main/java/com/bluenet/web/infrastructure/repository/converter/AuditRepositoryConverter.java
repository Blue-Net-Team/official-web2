package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import org.springframework.stereotype.Component;

/**
 * 审计日志仓储转换器
 * <p>
 * 负责 Audit 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AuditRepositoryConverter {

    /**
     * Entity → DO（用于保存）
     */
    public AuditDO toDataObject(Audit entity) {
        if (entity == null) {
            return null;
        }
        return AuditDO.builder()
                .id(entity.getId())
                .requestMethod(entity.getRequestMethod())
                .requestUri(entity.getRequestUri())
                .requestUriPattern(entity.getRequestUriPattern())
                .actionArg(entity.getActionArg())
                .actionUserId(entity.getActionUserId())
                .actionTime(entity.getActionTime())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .httpStatus(entity.getHttpStatus())
                .responseMessage(entity.getResponseMessage())
                .stackTrace(entity.getStackTrace())
                .durationMs(entity.getDurationMs())
                .successState(entity.getSuccessState())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Audit toEntity(AuditDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Audit.reconstruct(
                dataObject.getId(),
                dataObject.getRequestMethod(),
                dataObject.getRequestUri(),
                dataObject.getRequestUriPattern(),
                dataObject.getActionArg(),
                dataObject.getActionUserId(),
                dataObject.getActionTime(),
                dataObject.getIpAddress(),
                dataObject.getUserAgent(),
                dataObject.getHttpStatus(),
                dataObject.getResponseMessage(),
                dataObject.getStackTrace(),
                dataObject.getDurationMs(),
                dataObject.getSuccessState());
    }
}
