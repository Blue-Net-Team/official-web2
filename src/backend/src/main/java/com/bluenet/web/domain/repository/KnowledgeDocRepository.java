package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeDoc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 知识库文档仓储接口。
 */
public interface KnowledgeDocRepository {

    /**
     * 分页查询所有文档。
     *
     * @param pageable
     *            分页参数
     */
    Page<KnowledgeDoc> findAll(Pageable pageable);

    /**
     * 按主键查询文档。
     *
     * @param id
     *            文档ID
     */
    Optional<KnowledgeDoc> findById(Long id);

    /**
     * 保存文档。
     *
     * @param doc
     *            文档实体
     */
    void save(KnowledgeDoc doc);

    /**
     * 按主键删除文档。
     *
     * @param id
     *            文档ID
     */
    void deleteById(Long id);

    /**
     * 分段计数原子递减 1（用于删除单个分段后同步冗余列）。
     *
     * @param id
     *            文档ID
     */
    void decrementChunkCount(Long id);

}
