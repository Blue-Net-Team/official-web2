package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeDocDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库文档仓储转换器。
 */
@Component
public class KnowledgeDocRepositoryConverter {

    /**
     * Entity → DO
     */
    public KnowledgeDocDO toDataObject(KnowledgeDoc entity) {
        if (entity == null) {
            return null;
        }
        return KnowledgeDocDO.builder()
                .id(entity.getId())
                .fileId(entity.getFileId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .chunkCount(entity.getChunkCount())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DO → Entity
     */
    public KnowledgeDoc toEntity(KnowledgeDocDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return KnowledgeDoc.reconstruct(
                dataObject.getId(),
                dataObject.getFileId(),
                dataObject.getTitle(),
                dataObject.getStatus(),
                dataObject.getChunkCount(),
                dataObject.getErrorMessage(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<KnowledgeDoc> toEntityList(List<KnowledgeDocDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
