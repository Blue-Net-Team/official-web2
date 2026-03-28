package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.MessageTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {
}
