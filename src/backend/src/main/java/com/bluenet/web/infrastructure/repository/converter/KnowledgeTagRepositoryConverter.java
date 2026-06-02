package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.KnowledgeTag;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeTagDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库标签仓储转换器。
 */
@Component
public class KnowledgeTagRepositoryConverter {

    /**
     * Entity → DO
     */
    public KnowledgeTagDO toDataObject(KnowledgeTag entity) {
        if (entity == null) {
            return null;
        }
        return KnowledgeTagDO.builder()
                .id(entity.getId())
                .tagName(entity.getTagName())
                .tagDescription(entity.getTagDescription())
                .chunksCount(entity.getChunksCount())
                .build();
    }

    /**
     * DO → Entity
     */
    public KnowledgeTag toEntity(KnowledgeTagDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return KnowledgeTag.reconstruct(
                dataObject.getId(),
                dataObject.getTagName(),
                dataObject.getTagDescription(),
                dataObject.getChunksCount());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<KnowledgeTag> toEntityList(List<KnowledgeTagDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
