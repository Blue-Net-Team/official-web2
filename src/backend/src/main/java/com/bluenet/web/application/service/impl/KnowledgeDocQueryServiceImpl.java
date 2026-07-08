package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.knowledge.KnowledgeChunkResult;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.result.knowledge.KnowledgeTagResult;
import com.bluenet.web.application.service.KnowledgeDocQueryService;
import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.entity.KnowledgeTag;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.domain.repository.KnowledgeDocRepository;
import com.bluenet.web.domain.repository.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库文档查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeDocQueryServiceImpl implements KnowledgeDocQueryService {

    private final KnowledgeDocRepository knowledgeDocRepository;
    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final KnowledgeTagRepository knowledgeTagRepository;

    @Override
    public org.springframework.data.domain.Page<KnowledgeDocResult.ListItem> listDocuments(Pageable pageable) {
        org.springframework.data.domain.Page<KnowledgeDoc> result = knowledgeDocRepository.findAll(pageable);

        List<KnowledgeDocResult.ListItem> items = result.getContent()
                .stream()
                .map(this::toListItem)
                .toList();

        return new PageImpl<>(items, pageable, result.getTotalElements());
    }

    @Override
    public KnowledgeDocResult.Detail getDocumentDetail(Long docId) {
        KnowledgeDoc entity = knowledgeDocRepository.findById(docId).orElse(null);
        if (entity == null) {
            return null;
        }
        return toDetail(entity);
    }

    @Override
    public org.springframework.data.domain.Page<KnowledgeChunkResult.ListItem> listChunks(Long docId,
            Pageable pageable) {
        org.springframework.data.domain.Page<KnowledgeChunk> result = knowledgeChunkRepository
                .findByDocId(docId, pageable);

        List<KnowledgeChunkResult.ListItem> items = result.getContent()
                .stream()
                .map(this::toChunkItem)
                .toList();

        return new PageImpl<>(items, pageable, result.getTotalElements());
    }

    @Override
    public org.springframework.data.domain.Page<KnowledgeTagResult.ListItem> listTags(Pageable pageable) {
        org.springframework.data.domain.Page<KnowledgeTag> result = knowledgeTagRepository.findAll(pageable);

        List<KnowledgeTagResult.ListItem> items = result.getContent()
                .stream()
                .map(this::toTagItem)
                .toList();

        return new PageImpl<>(items, pageable, result.getTotalElements());
    }

    private KnowledgeDocResult.ListItem toListItem(KnowledgeDoc entity) {
        return new KnowledgeDocResult.ListItem(
                entity.getId(),
                entity.getFileId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getChunkCount(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private KnowledgeDocResult.Detail toDetail(KnowledgeDoc entity) {
        return new KnowledgeDocResult.Detail(
                entity.getId(),
                entity.getFileId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getChunkCount(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private KnowledgeChunkResult.ListItem toChunkItem(KnowledgeChunk entity) {
        return new KnowledgeChunkResult.ListItem(
                entity.getId(),
                entity.getDocId(),
                entity.getContent(),
                entity.getTags(),
                entity.getSource());
    }

    private KnowledgeTagResult.ListItem toTagItem(KnowledgeTag entity) {
        return new KnowledgeTagResult.ListItem(
                entity.getId(),
                entity.getTagName(),
                entity.getTagDescription(),
                entity.getChunksCount());
    }
}
