package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库分片-标签关联中间表 Mapper。
 */
@Mapper
public interface KnowledgeChunkTagMapper extends BaseMapper<KnowledgeChunkTagDO> {

    /**
     * 按分段ID删除全部关联。
     *
     * @param chunkId
     *            分段ID
     * @return 影响行数
     */
    int deleteByChunkId(@Param("chunkId") Long chunkId);

    /**
     * 按标签ID删除全部关联。
     *
     * @param tagId
     *            标签ID
     * @return 影响行数
     */
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 按分段ID列表批量查询关联的标签ID。
     *
     * @param chunkIds
     *            分段ID列表
     * @return 关联记录列表
     */
    List<KnowledgeChunkTagDO> selectByChunkIds(@Param("chunkIds") List<Long> chunkIds);

    /**
     * 按文档ID删除该文档下所有分片的关联（通过 join tb_rag_chunks）。
     *
     * @param docId
     *            文档ID
     * @return 影响行数
     */
    int deleteByDocId(@Param("docId") Long docId);

    /**
     * 统计标签关联的分段数。
     *
     * @param tagId
     *            标签ID
     * @return 关联分段数
     */
    long countByTagId(@Param("tagId") Long tagId);
}
