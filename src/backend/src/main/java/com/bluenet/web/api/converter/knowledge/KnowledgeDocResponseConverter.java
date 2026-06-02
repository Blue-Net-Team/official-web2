package com.bluenet.web.api.converter.knowledge;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.knowledge.*;
import com.bluenet.web.application.knowledge.KnowledgeChunkResult;
import com.bluenet.web.application.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.knowledge.KnowledgeTagResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * 知识库文档响应转换器。
 */
@Component
public class KnowledgeDocResponseConverter {

    public PageDTO<KnowledgeDocListItemResponseDTO> toDocListPageDTO(Page<KnowledgeDocResult.ListItem> page) {
        return PageDTO.from(page.map(this::toListItemDTO));
    }

    public KnowledgeDocListItemResponseDTO toListItemDTO(KnowledgeDocResult.ListItem item) {
        return new KnowledgeDocListItemResponseDTO(
                item.id(), item.fileId(), item.title(), item.status(),
                item.chunkCount(), item.errorMessage(), item.createdAt(), item.updatedAt());
    }

    public KnowledgeDocDetailResponseDTO toDetailDTO(KnowledgeDocResult.Detail detail) {
        return new KnowledgeDocDetailResponseDTO(
                detail.id(), detail.fileId(), detail.title(), detail.status(),
                detail.chunkCount(), detail.errorMessage(), detail.createdAt(), detail.updatedAt());
    }

    public PageDTO<KnowledgeChunkListItemResponseDTO> toChunkListPageDTO(Page<KnowledgeChunkResult.ListItem> page) {
        return PageDTO.from(page.map(this::toChunkDTO));
    }

    public KnowledgeChunkListItemResponseDTO toChunkDTO(KnowledgeChunkResult.ListItem item) {
        return new KnowledgeChunkListItemResponseDTO(
                item.id(), item.docId(), item.content(), item.tags(), item.source());
    }

    public PageDTO<KnowledgeTagListItemResponseDTO> toTagListPageDTO(Page<KnowledgeTagResult.ListItem> page) {
        return PageDTO.from(page.map(this::toTagDTO));
    }

    public KnowledgeTagListItemResponseDTO toTagDTO(KnowledgeTagResult.ListItem item) {
        return new KnowledgeTagListItemResponseDTO(
                item.id(), item.tagName(), item.tagDescription(), item.chunksCount());
    }
}
