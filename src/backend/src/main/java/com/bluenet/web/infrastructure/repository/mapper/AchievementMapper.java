package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
}
