package com.bluenet.web.application.service;

import com.bluenet.web.application.knowledge.KnowledgeChunkResult;
import com.bluenet.web.application.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.knowledge.KnowledgeTagResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 知识库文档查询服务接口。
 */
public interface KnowledgeDocQueryService {

    /**
     * 分页查询文档列表。
     *
     * @param pageable
     *            分页参数
     * @return 文档列表分页结果
     */
    Page<KnowledgeDocResult.ListItem> listDocuments(Pageable pageable);

    /**
     * 查询文档详情。
     *
     * @param docId
     *            文档ID
     * @return 文档详情
     */
    KnowledgeDocResult.Detail getDocumentDetail(Long docId);

    /**
     * 分页查询文档分段。
     *
     * @param docId
     *            文档ID
     * @param pageable
     *            分页参数
     * @return 分段列表分页结果
     */
    Page<KnowledgeChunkResult.ListItem> listChunks(Long docId, Pageable pageable);

    /**
     * 分页查询标签列表。
     *
     * @param pageable
     *            分页参数
     * @return 标签列表分页结果
     */
    Page<KnowledgeTagResult.ListItem> listTags(Pageable pageable);
}
