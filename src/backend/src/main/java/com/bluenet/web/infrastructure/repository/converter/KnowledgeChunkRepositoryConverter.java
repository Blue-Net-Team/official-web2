package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.domain.model.enumerate.ChunkVectorStatus;
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
                .vectorStatus(entity.getVectorStatus() != null ? entity.getVectorStatus().getValue() : null)
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
                List.of(),
                dataObject.getSource(),
                parseVectorStatus(dataObject.getVectorStatus()));
    }

    /**
     * DO 列表 → Entity 列表（tagIds 由仓储层批量填充）
     */
    public List<KnowledgeChunk> toEntityList(List<KnowledgeChunkDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }

    private ChunkVectorStatus parseVectorStatus(String value) {
        if (value == null) {
            return ChunkVectorStatus.SYNCED;
        }
        for (ChunkVectorStatus status : ChunkVectorStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ChunkVectorStatus.SYNCED;
    }
}
