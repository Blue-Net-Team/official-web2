package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeJobDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 算法评测任务仓储转换器
 * <p>
 * 负责 AlgorithmJudgeJob 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AlgorithmJudgeJobRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AlgorithmJudgeJobDO toDataObject(AlgorithmJudgeJob entity) {
        if (entity == null) {
            return null;
        }
        return AlgorithmJudgeJobDO.builder()
                .id(entity.getId())
                .answerId(entity.getAnswerId())
                .questionId(entity.getQuestionId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .userId(entity.getUserId())
                .language(entity.getLanguage())
                .sourceCode(entity.getSourceCode())
                .testcaseType(entity.getTestcaseType())
                .customInput(entity.getCustomInput())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .maxRetryCount(entity.getMaxRetryCount())
                .statusMessage(entity.getStatusMessage())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AlgorithmJudgeJob toEntity(AlgorithmJudgeJobDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AlgorithmJudgeJob.reconstruct(
                dataObject.getId(),
                dataObject.getAnswerId(),
                dataObject.getQuestionId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getUserId(),
                dataObject.getLanguage(),
                dataObject.getSourceCode(),
                dataObject.getTestcaseType(),
                dataObject.getCustomInput(),
                dataObject.getStatus(),
                dataObject.getRetryCount(),
                dataObject.getMaxRetryCount(),
                dataObject.getStatusMessage(),
                dataObject.getStartedAt(),
                dataObject.getFinishedAt(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<AlgorithmJudgeJob> toEntityList(List<AlgorithmJudgeJobDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
