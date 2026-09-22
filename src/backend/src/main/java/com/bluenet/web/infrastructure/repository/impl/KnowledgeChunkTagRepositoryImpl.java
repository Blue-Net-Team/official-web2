package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.repository.KnowledgeChunkTagRepository;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkTagDO;
import com.bluenet.web.infrastructure.repository.mapper.KnowledgeChunkTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库分片-标签关联中间表仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeChunkTagRepositoryImpl implements KnowledgeChunkTagRepository {

    private final KnowledgeChunkTagMapper knowledgeChunkTagMapper;

    @Override
    public void replaceByChunkId(Long chunkId, List<Long> tagIds) {
        knowledgeChunkTagMapper.deleteByChunkId(chunkId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            knowledgeChunkTagMapper.insert(new KnowledgeChunkTagDO(chunkId, tagId));
        }
    }

    @Override
    public void deleteByTagId(Long tagId) {
        knowledgeChunkTagMapper.deleteByTagId(tagId);
    }

    @Override
    public void deleteByDocId(Long docId) {
        knowledgeChunkTagMapper.deleteByDocId(docId);
    }

    @Override
    public long countByTagId(Long tagId) {
        return knowledgeChunkTagMapper.countByTagId(tagId);
    }
}
