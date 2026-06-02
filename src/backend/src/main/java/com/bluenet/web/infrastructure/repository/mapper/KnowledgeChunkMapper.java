package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeChunkDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库分段 Mapper。
 */
@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunkDO> {

    /**
     * 按文档ID删除分段。
     *
     * @param docId
     *            文档ID
     * @return 影响行数
     */
    int deleteByDocId(@Param("docId") Long docId);

    /**
     * 按文档ID查询分段列表。
     *
     * @param docId
     *            文档ID
     * @return 分段列表
     */
    List<KnowledgeChunkDO> selectByDocId(@Param("docId") Long docId);

    /**
     * 按文档ID分页查询分段列表（使用 BaseResultMap，确保 typeHandler 生效）。
     *
     * @param page
     *            分页对象
     * @param docId
     *            文档ID
     * @return 分页结果
     */
    Page<KnowledgeChunkDO> selectPageByDocId(Page<KnowledgeChunkDO> page, @Param("docId") Long docId);
}
