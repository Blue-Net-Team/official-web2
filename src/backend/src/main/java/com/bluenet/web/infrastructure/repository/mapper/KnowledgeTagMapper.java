package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 知识库标签 Mapper。
 */
@Mapper
public interface KnowledgeTagMapper extends BaseMapper<KnowledgeTagDO> {

    /**
     * 更新标签描述。
     *
     * @param tagId
     *            标签ID
     * @param description
     *            新描述
     * @return 影响行数
     */
    @Update("UPDATE tb_rag_tags SET tag_description = #{description} WHERE id = #{tagId}")
    int updateDescription(@Param("tagId") Long tagId, @Param("description") String description);
}
