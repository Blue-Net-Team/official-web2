package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {
    IPage<AchievementVO> selectAchievementsWithFilter(
            @Param("type") AchievementType type,
            @Param("awardLevel") AwardLevel awardLevel,
            @Param("year") Integer year,
            Page<AchievementVO> page);

    AchievementStatsVO selectAchievementStats();

    Long countByTypeAndAwardLevel(
            @Param("type") AchievementType type,
            @Param("awardLevel") AwardLevel awardLevel);
}
