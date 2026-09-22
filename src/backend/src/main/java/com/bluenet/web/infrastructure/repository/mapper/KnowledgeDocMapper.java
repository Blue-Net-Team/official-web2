package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeDocDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 知识库文档 Mapper。
 */
@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDocDO> {

    /**
     * 分段计数原子递减 1。
     *
     * @param id
     *            文档ID
     * @return 影响行数
     */
    @Update("UPDATE tb_rag_docs SET chunk_count = chunk_count - 1 WHERE id = #{id}")
    int decrementChunkCount(@Param("id") Long id);
}
