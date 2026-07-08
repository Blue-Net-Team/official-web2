package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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
     * @return 分页后的成果结果。
     */
    Page<AchievementReadModel> findAchievementsWithFilter(AchievementType type, AwardLevel awardLevel, Integer year,
            Pageable pageable);

    /**
     * 统计各级别成果数量和成果总数。
     *
     * @return 查询或处理得到的成果结果。
     */
    AchievementStatistics findAchievementStats();

    /**
     * 保存新的成果记录。
     *
     * @param achievement
     *            成果实体。
     */
    void save(Achievement achievement);

    /**
     * 按主键查询成果记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的成就实体；不存在时为 Optional.empty()。
     */
    Optional<Achievement> findById(Long id);

    /**
     * 删除指定成果记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);
}
