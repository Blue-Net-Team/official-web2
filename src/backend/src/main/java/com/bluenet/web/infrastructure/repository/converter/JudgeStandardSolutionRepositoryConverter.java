package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.JudgeStandardSolution;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeStandardSolutionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标准解仓储转换器。
 */
@Component
public class JudgeStandardSolutionRepositoryConverter {

    /**
     * Entity → DO。
     *
     * @param entity
     *            领域实体。
     * @return 数据对象。
     */
    public JudgeStandardSolutionDO toDataObject(JudgeStandardSolution entity) {
        if (entity == null) {
            return null;
        }
        return JudgeStandardSolutionDO.builder()
                .id(entity.getId())
                .configId(entity.getConfigId())
                .questionId(entity.getQuestionId())
                .language(entity.getLanguage())
                .objectKey(entity.getObjectKey())
                .objectHash(entity.getObjectHash())
                .primarySolution(entity.getPrimarySolution())
                .benchmarkStatus(entity.getBenchmarkStatus())
                .p95TimeMs(entity.getP95TimeMs())
                .maxTimeMs(entity.getMaxTimeMs())
                .peakMemoryKb(entity.getPeakMemoryKb())
                .suggestedTimeLimitMs(entity.getSuggestedTimeLimitMs())
                .benchmarkMessage(entity.getBenchmarkMessage())
                .build();
    }

    /**
     * DO → Entity。
     *
     * @param dataObject
     *            数据对象。
     * @return 领域实体。
     */
    public JudgeStandardSolution toEntity(JudgeStandardSolutionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return JudgeStandardSolution.reconstruct(
                dataObject.getId(),
                dataObject.getConfigId(),
                dataObject.getQuestionId(),
                dataObject.getLanguage(),
                dataObject.getObjectKey(),
                dataObject.getObjectHash(),
                dataObject.getPrimarySolution(),
                dataObject.getBenchmarkStatus(),
                dataObject.getP95TimeMs(),
                dataObject.getMaxTimeMs(),
                dataObject.getPeakMemoryKb(),
                dataObject.getSuggestedTimeLimitMs(),
                dataObject.getBenchmarkMessage());
    }

    /**
     * DO 列表 → Entity 列表。
     *
     * @param dataObjects
     *            数据对象列表。
     * @return 领域实体列表。
     */
    public List<JudgeStandardSolution> toEntityList(List<JudgeStandardSolutionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
