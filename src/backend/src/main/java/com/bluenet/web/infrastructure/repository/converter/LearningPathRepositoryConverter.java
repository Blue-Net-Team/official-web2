package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.infrastructure.repository.dataobject.DirectionLearningStepDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学习路径仓储转换器
 * <p>
 * 负责 DirectionLearningStep 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class LearningPathRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public DirectionLearningStepDO toDataObject(DirectionLearningStep entity) {
        if (entity == null) {
            return null;
        }
        return DirectionLearningStepDO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .stepNumber(entity.getStepNumber())
                .title(entity.getTitle())
                .relatedUrl(entity.getRelatedUrl())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public DirectionLearningStep toEntity(DirectionLearningStepDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return DirectionLearningStep.reconstruct(
                dataObject.getId(),
                dataObject.getDirection(),
                dataObject.getStepNumber(),
                dataObject.getTitle(),
                dataObject.getRelatedUrl());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<DirectionLearningStep> toEntityList(List<DirectionLearningStepDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
