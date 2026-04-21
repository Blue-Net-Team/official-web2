package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.UserAchievementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievementDO> {
}
