package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.projection.AchievementStatsProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AchievementMapper extends BaseMapper<AchievementDO> {
    /**
     * 按成果类型和奖项级别分页查询成果数据行。
     *
     * @param type
     *            业务类型或枚举类型。
     * @param awardLevel
     *            成果奖项级别过滤条件。
     * @param year
     *            成果取得年份过滤条件。
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @return 分页后的成果 结果。
     */
    IPage<AchievementDO> selectAchievementsWithFilter(
            @Param("type") AchievementType type,
            @Param("awardLevel") AwardLevel awardLevel,
            @Param("year") Integer year,
            Page<AchievementDO> page);

    /**
     * 查询成果 数据行。
     *
     * @return 匹配条件的成果 数据行；不存在时为 null。
     */
    AchievementStatsProjection selectAchievementStats();

    /**
     * 统计指定成果类型和奖项级别下的成果数量。
     *
     * @param type
     *            业务类型或枚举类型。
     * @param awardLevel
     *            成果奖项级别过滤条件。
     * @return 满足条件的记录数量。
     */
    Long countByTypeAndAwardLevel(
            @Param("type") AchievementType type,
            @Param("awardLevel") AwardLevel awardLevel);
}
