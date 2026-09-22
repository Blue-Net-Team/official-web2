package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.infrastructure.repository.converter.KnowledgeChunkRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkTagDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkMapper;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库分段仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeChunkRepositoryImpl implements KnowledgeChunkRepository {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final KnowledgeChunkTagMapper knowledgeChunkTagMapper;
    private final KnowledgeChunkRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<KnowledgeChunk> findByDocId(Long docId, Pageable pageable) {
        Page<KnowledgeChunkDO> mpPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        Page<KnowledgeChunkDO> result = knowledgeChunkMapper.selectPageByDocId(mpPage, docId);
        List<KnowledgeChunk> items = converter.toEntityList(result.getRecords());
        fillTagIds(items);
        return new PageImpl<>(items, pageable, result.getTotal());
    }

    @Override
    public Optional<KnowledgeChunk> findById(Long id) {
        KnowledgeChunkDO dataObject = knowledgeChunkMapper.selectById(id);
        if (dataObject == null) {
            return Optional.empty();
        }
        KnowledgeChunk entity = converter.toEntity(dataObject);
        fillTagIds(List.of(entity));
        return Optional.of(entity);
    }

    @Override
    public void updateContent(KnowledgeChunk chunk) {
        KnowledgeChunkDO dataObject = new KnowledgeChunkDO();
        dataObject.setId(chunk.getId());
        dataObject.setContent(chunk.getContent());
        dataObject.setVectorStatus(
                chunk.getVectorStatus() != null ? chunk.getVectorStatus().getValue() : null);
        knowledgeChunkMapper.updateById(dataObject);
    }

    @Override
    public void deleteByDocId(Long docId) {
        knowledgeChunkMapper.deleteByDocId(docId);
    }

    /**
     * 批量填充分片实体的标签ID列表。
     */
    private void fillTagIds(List<KnowledgeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        List<Long> chunkIds = chunks.stream().map(KnowledgeChunk::getId).toList();
        Map<Long, List<Long>> tagIdsByChunk = knowledgeChunkTagMapper.selectByChunkIds(chunkIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                KnowledgeChunkTagDO::getChunkId,
                                Collectors.mapping(KnowledgeChunkTagDO::getTagId, Collectors.toList())));
        for (KnowledgeChunk chunk : chunks) {
            chunk.setTagIds(tagIdsByChunk.getOrDefault(chunk.getId(), List.of()));
        }
    }
}
