package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import com.bluenet.web.domain.repository.KnowledgeChunkRepository;
import com.bluenet.web.infrastructure.repository.converter.KnowledgeChunkRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库分段仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeChunkRepositoryImpl implements KnowledgeChunkRepository {

    private final KnowledgeChunkMapper knowledgeChunkMapper;
    private final KnowledgeChunkRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<KnowledgeChunk> findByDocId(Long docId, Pageable pageable) {
        Page<KnowledgeChunkDO> mpPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        Page<KnowledgeChunkDO> result = knowledgeChunkMapper.selectPageByDocId(mpPage, docId);
        List<KnowledgeChunk> items = converter.toEntityList(result.getRecords());
        return new PageImpl<>(items, pageable, result.getTotal());
    }

    @Override
    public void deleteByDocId(Long docId) {
        knowledgeChunkMapper.deleteByDocId(docId);
    }
}
