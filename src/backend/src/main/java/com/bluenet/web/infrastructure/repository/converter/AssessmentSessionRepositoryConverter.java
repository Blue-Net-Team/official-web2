package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentSessionDO;
import org.springframework.stereotype.Component;

/**
 * 考核会话仓储转换器
 * <p>
 * 负责 AssessmentSession 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AssessmentSessionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AssessmentSessionDO toDataObject(AssessmentSession entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentSessionDO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .startTime(entity.getStartTime())
                .deadline(entity.getDeadline())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AssessmentSession toEntity(AssessmentSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentSession.reconstruct(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getStartTime(),
                dataObject.getDeadline());
    }
}
