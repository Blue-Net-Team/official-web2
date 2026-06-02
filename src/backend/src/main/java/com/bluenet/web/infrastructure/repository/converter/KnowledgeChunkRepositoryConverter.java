package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库分段仓储转换器。
 */
@Component
public class KnowledgeChunkRepositoryConverter {

    /**
     * Entity → DO
     */
    public KnowledgeChunkDO toDataObject(KnowledgeChunk entity) {
        if (entity == null) {
            return null;
        }
        return KnowledgeChunkDO.builder()
                .id(entity.getId())
                .docId(entity.getDocId())
                .content(entity.getContent())
                .tags(entity.getTags())
                .source(entity.getSource())
                .build();
    }

    /**
     * DO → Entity
     */
    public KnowledgeChunk toEntity(KnowledgeChunkDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return KnowledgeChunk.reconstruct(
                dataObject.getId(),
                dataObject.getDocId(),
                dataObject.getContent(),
                dataObject.getTags(),
                dataObject.getSource());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<KnowledgeChunk> toEntityList(List<KnowledgeChunkDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
