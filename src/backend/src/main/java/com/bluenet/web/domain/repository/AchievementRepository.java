package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AchievementRepository {
    /**
     * 按成果类型和奖项级别分页查询成果视图。
     *
     * @param type
     *            业务类型或枚举类型。
     * @param awardLevel
     *            成果奖项级别过滤条件。
     * @param year
     *            成果取得年份过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的成果 结果。
     */
    Page<AchievementVO> findAchievementsWithFilter(AchievementType type, AwardLevel awardLevel, Integer year,
            Pageable pageable);

    /**
     * 统计各级别成果数量和成果总数。
     *
     * @return 查询或处理得到的成果 结果。
     */
    AchievementStatsVO findAchievementStats();

    /**
     * 保存新的成果 记录。
     *
     * @param achievement
     *            成果领域对象。
     * @return 查询或处理得到的成果 结果。
     */
    AchievementVO save(Achievement achievement);

    /**
     * 按主键查询成果 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的成果 结果。
     */
    AchievementVO findById(Long id);

    /**
     * 更新已有成果 记录。
     *
     * @param achievement
     *            成果领域对象。
     * @return 数据库受影响行数。
     */
    AchievementVO update(Achievement achievement);

    /**
     * 删除指定成果 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void delete(Long id);
}
