package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 考核时间仓储转换器
 * <p>
 * 负责 AssessmentTime 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AssessmentTimeRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AssessmentTimeDO toDataObject(AssessmentTime entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentTimeDO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .epoch(entity.getEpoch())
                .grade(entity.getGrade())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timeLimit(entity.getTimeLimit())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .resultsPublishedAt(entity.getResultsPublishedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public AssessmentTime toEntity(AssessmentTimeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentTime.reconstruct(
                dataObject.getId(),
                dataObject.getDirection(),
                dataObject.getEpoch(),
                dataObject.getGrade(),
                dataObject.getStartTime(),
                dataObject.getEndTime(),
                dataObject.getTimeLimit(),
                dataObject.getTimeLimitMinutes(),
                dataObject.getResultsPublishedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<AssessmentTime> toEntityList(List<AssessmentTimeDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
