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
     * 更新文档解析状态。
     *
     * @param id
     *            文档ID
     * @param status
     *            新状态
     * @param chunkCount
     *            分段数量
     * @param errorMessage
     *            错误信息
     * @return 影响行数
     */
    @Update("UPDATE tb_rag_docs SET status = #{status.value}, chunk_count = #{chunkCount}, error_message = #{errorMessage}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
            @Param("status") com.bluenet.web.domain.model.enumerate.DocParseStatus status,
            @Param("chunkCount") Integer chunkCount, @Param("errorMessage") String errorMessage);
}
