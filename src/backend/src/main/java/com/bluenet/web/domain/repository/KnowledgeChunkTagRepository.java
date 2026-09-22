package com.bluenet.web.domain.repository;

import java.util.List;

/**
 * 知识库分片-标签关联中间表仓储接口。
 */
public interface KnowledgeChunkTagRepository {

    /**
     * 重建指定分片的全部标签关联（先删后插）。
     *
     * @param chunkId
     *            分段ID
     * @param tagIds
     *            标签ID列表
     */
    void replaceByChunkId(Long chunkId, List<Long> tagIds);

    /**
     * 按标签ID删除全部关联。
     *
     * @param tagId
     *            标签ID
     */
    void deleteByTagId(Long tagId);

    /**
     * 按分段ID删除该分段的全部标签关联。
     *
     * @param chunkId
     *            分段ID
     */
    void deleteByChunkId(Long chunkId);

    /**
     * 按文档ID删除该文档下所有分片的关联。
     *
     * @param docId
     *            文档ID
     */
    void deleteByDocId(Long docId);

    /**
     * 查询标签当前关联的分段数。
     *
     * @param tagId
     *            标签ID
     * @return 关联分段数
     */
    long countByTagId(Long tagId);
}
