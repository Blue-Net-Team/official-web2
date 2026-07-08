package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.KnowledgeDoc;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;

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
     * 更新解析状态。
     *
     * @param id
     *            文档ID
     * @param status
     *            新状态
     * @param chunkCount
     *            分段数量
     * @param errorMessage
     *            错误信息
     */
    void updateStatus(Long id, DocParseStatus status, Integer chunkCount, String errorMessage);
}
