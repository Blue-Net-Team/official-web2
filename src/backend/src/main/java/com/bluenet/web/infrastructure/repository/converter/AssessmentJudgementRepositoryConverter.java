package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 考核评判仓储转换器
 * <p>
 * 负责 AssessmentJudgement 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AssessmentJudgementRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AssessmentJudgementDO toDataObject(AssessmentJudgement entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentJudgementDO.builder()
                .id(entity.getId())
                .answerId(entity.getAnswerId())
                .questionId(entity.getQuestionId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .userId(entity.getUserId())
                .score(entity.getScore())
                .maxScore(entity.getMaxScore())
                .status(entity.getStatus())
                .resultCode(entity.getResultCode())
                .source(entity.getSource())
                .reviewerId(entity.getReviewerId())
                .reviewerType(entity.getReviewerType())
                .comment(entity.getComment())
                .judgedAt(entity.getJudgedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AssessmentJudgement toEntity(AssessmentJudgementDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentJudgement.reconstruct(
                dataObject.getId(),
                dataObject.getAnswerId(),
                dataObject.getQuestionId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getUserId(),
                dataObject.getScore(),
                dataObject.getMaxScore(),
                dataObject.getStatus(),
                dataObject.getResultCode(),
                dataObject.getSource(),
                dataObject.getReviewerId(),
                dataObject.getReviewerType(),
                dataObject.getComment(),
                dataObject.getJudgedAt(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<AssessmentJudgement> toEntityList(List<AssessmentJudgementDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
