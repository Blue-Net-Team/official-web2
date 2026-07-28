package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementExternalMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AchievementExternalMemberMapper extends BaseMapper<AchievementExternalMemberDO> {
    /**
     * 按成就ID批量查询外部协作者，按展示顺序排序。
     *
     * @param achievementIds
     *            成就ID列表
     * @return 外部协作者列表
     */
    List<AchievementExternalMemberDO> selectByAchievementIds(@Param("achievementIds") List<Long> achievementIds);
}
