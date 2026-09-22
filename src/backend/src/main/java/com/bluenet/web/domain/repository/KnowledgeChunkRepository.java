package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 知识库分段仓储接口。
 */
public interface KnowledgeChunkRepository {

    /**
     * 按文档ID分页查询分段列表（含标签ID解析）。
     *
     * @param docId
     *            文档ID
     * @param pageable
     *            分页参数
     * @return 分段分页结果
     */
    Page<KnowledgeChunk> findByDocId(Long docId, Pageable pageable);

    /**
     * 按主键查询分段。
     *
     * @param id
     *            分段ID
     * @return 查询到的分段；不存在时为空
     */
    Optional<KnowledgeChunk> findById(Long id);

    /**
     * 更新分段内容与向量同步状态（不写标签关联，关联由 {@link KnowledgeChunkTagRepository} 维护）。
     *
     * @param chunk
     *            分段实体（id 必须非空）
     */
    void updateContent(KnowledgeChunk chunk);

    /**
     * 按文档ID删除所有分段。
     *
     * @param docId
     *            文档ID
     */
    void deleteByDocId(Long docId);

    /**
     * 按主键删除单个分段。
     *
     * @param id
     *            分段ID
     */
    void deleteById(Long id);
}
