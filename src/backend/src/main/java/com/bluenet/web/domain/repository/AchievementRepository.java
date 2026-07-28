package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.domain.model.readmodel.AchievementMemberReadModel;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
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
     * <p>
     * 级联清理成员关联与外部协作者记录。
     * </p>
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 全量替换成就的系统内成员关联。
     *
     * @param achievementId
     *            成就ID。
     * @param userIds
     *            系统内成员ID列表，空列表表示清空关联。
     */
    void saveMemberAssociations(Long achievementId, List<Long> userIds);

    /**
     * 全量替换成就的外部协作者。
     *
     * @param achievementId
     *            成就ID。
     * @param names
     *            外部协作者姓名列表，空列表表示清空。
     */
    void saveExternalMembers(Long achievementId, List<String> names);

    /**
     * 按成就ID批量查询系统内成员简要信息。
     *
     * @param achievementIds
     *            成就ID列表。
     * @return 成就ID到成员简要信息列表的映射。
     */
    Map<Long, List<AchievementMemberReadModel>> findMembersByAchievementIds(List<Long> achievementIds);

    /**
     * 按成就ID批量查询外部协作者姓名。
     *
     * @param achievementIds
     *            成就ID列表。
     * @return 成就ID到外部协作者姓名列表的映射，按展示顺序排序。
     */
    Map<Long, List<String>> findExternalMembersByAchievementIds(List<Long> achievementIds);

    /**
     * 查询指定用户关联的成就列表，按获奖日期降序。
     *
     * @param userId
     *            用户ID。
     * @return 成就读模型列表，包含成员与外部协作者信息。
     */
    List<AchievementReadModel> findByUserId(Long userId);
}
