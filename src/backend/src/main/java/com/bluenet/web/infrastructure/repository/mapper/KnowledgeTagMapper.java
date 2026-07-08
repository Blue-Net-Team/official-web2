package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeTagDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库标签 Mapper。
 */
@Mapper
public interface KnowledgeTagMapper extends BaseMapper<KnowledgeTagDO> {
}
