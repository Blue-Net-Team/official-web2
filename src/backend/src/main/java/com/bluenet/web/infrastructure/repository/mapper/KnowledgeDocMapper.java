package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.KnowledgeDocDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper。
 */
@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDocDO> {
}
