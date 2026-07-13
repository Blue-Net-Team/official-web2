package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeTestcaseConfigDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试用例生成配置仓储转换器。
 */
@Component
public class JudgeTestcaseConfigRepositoryConverter {

    /**
     * Entity → DO。
     *
     * @param entity
     *            领域实体。
     * @return 数据对象。
     */
    public JudgeTestcaseConfigDO toDataObject(JudgeTestcaseConfig entity) {
        if (entity == null) {
            return null;
        }
        return JudgeTestcaseConfigDO.builder()
                .id(entity.getId())
                .configId(entity.getConfigId())
                .caseNo(entity.getCaseNo())
                .category(entity.getCategory())
                .generatorArgs(entity.getGeneratorArgs())
                .weight(entity.getWeight())
                .hidden(entity.getHidden())
                .sample(entity.getSample())
                .description(entity.getDescription())
                .build();
    }

    /**
     * DO → Entity。
     *
     * @param dataObject
     *            数据对象。
     * @return 领域实体。
     */
    public JudgeTestcaseConfig toEntity(JudgeTestcaseConfigDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return JudgeTestcaseConfig.reconstruct(
                dataObject.getId(),
                dataObject.getConfigId(),
                dataObject.getCaseNo(),
                dataObject.getCategory(),
                dataObject.getGeneratorArgs(),
                dataObject.getWeight(),
                dataObject.getHidden(),
                dataObject.getSample(),
                dataObject.getDescription());
    }

    /**
     * DO 列表 → Entity 列表。
     *
     * @param dataObjects
     *            数据对象列表。
     * @return 领域实体列表。
     */
    public List<JudgeTestcaseConfig> toEntityList(List<JudgeTestcaseConfigDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
