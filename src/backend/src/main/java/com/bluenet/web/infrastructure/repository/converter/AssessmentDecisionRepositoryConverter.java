package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentDecisionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 考核决策仓储转换器
 * <p>
 * 负责 AssessmentDecision 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AssessmentDecisionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AssessmentDecisionDO toDataObject(AssessmentDecision entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentDecisionDO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .passed(entity.getPassed())
                .decidedBy(entity.getDecidedBy())
                .decisionComment(entity.getDecisionComment())
                .decidedAt(entity.getDecidedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AssessmentDecision toEntity(AssessmentDecisionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentDecision.reconstruct(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getPassed(),
                dataObject.getDecidedBy(),
                dataObject.getDecisionComment(),
                dataObject.getDecidedAt(),
                dataObject.getUpdatedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<AssessmentDecision> toEntityList(List<AssessmentDecisionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
