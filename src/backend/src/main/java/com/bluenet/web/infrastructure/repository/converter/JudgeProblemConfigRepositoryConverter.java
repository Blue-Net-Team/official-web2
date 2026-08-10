package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.JudgeProblemConfig;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 判题配置仓储转换器。
 */
@Component
public class JudgeProblemConfigRepositoryConverter {

    /**
     * Entity → DO。
     *
     * @param entity
     *            领域实体。
     * @return 数据对象。
     */
    public JudgeProblemConfigDO toDataObject(JudgeProblemConfig entity) {
        if (entity == null) {
            return null;
        }
        return JudgeProblemConfigDO.builder()
                .id(entity.getId())
                .questionId(entity.getQuestionId())
                .generatorLanguage(entity.getGeneratorLanguage())
                .generatorObjectKey(entity.getGeneratorObjectKey())
                .generatorObjectHash(entity.getGeneratorObjectHash())
                .manifestObjectKey(entity.getManifestObjectKey())
                .manifestObjectHash(entity.getManifestObjectHash())
                .primaryStandardLanguage(entity.getPrimaryStandardLanguage())
                .status(entity.getStatus())
                .benchmarkRepeatTimes(entity.getBenchmarkRepeatTimes())
                .marginMultiplier(entity.getMarginMultiplier())
                .minExtraMs(entity.getMinExtraMs())
                .roundToMs(entity.getRoundToMs())
                .build();
    }

    /**
     * DO → Entity。
     *
     * @param dataObject
     *            数据对象。
     * @return 领域实体。
     */
    public JudgeProblemConfig toEntity(JudgeProblemConfigDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return JudgeProblemConfig.reconstruct(
                dataObject.getId(),
                dataObject.getQuestionId(),
                dataObject.getGeneratorLanguage(),
                dataObject.getGeneratorObjectKey(),
                dataObject.getGeneratorObjectHash(),
                dataObject.getManifestObjectKey(),
                dataObject.getManifestObjectHash(),
                dataObject.getPrimaryStandardLanguage(),
                dataObject.getStatus(),
                dataObject.getBenchmarkRepeatTimes(),
                dataObject.getMarginMultiplier(),
                dataObject.getMinExtraMs(),
                dataObject.getRoundToMs());
    }

    /**
     * DO 列表 → Entity 列表。
     *
     * @param dataObjects
     *            数据对象列表。
     * @return 领域实体列表。
     */
    public List<JudgeProblemConfig> toEntityList(List<JudgeProblemConfigDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
