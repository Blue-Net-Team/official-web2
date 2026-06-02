package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 知识库分段仓储接口。
 */
public interface KnowledgeChunkRepository {

    /**
     * 按文档ID分页查询分段列表。
     *
     * @param docId
     *            文档ID
     * @param pageable
     *            分页参数
     * @return 分段分页结果
     */
    Page<KnowledgeChunk> findByDocId(Long docId, Pageable pageable);

    /**
     * 按文档ID删除所有分段。
     *
     * @param docId
     *            文档ID
     */
    void deleteByDocId(Long docId);
}
