package com.bluenet.web.application.service;

import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.application.command.achievement.AchievementCommands;

/**
 * 成就应用服务接口。
 * <p>
 * 定义了成就聚合在应用层的所有业务操作。
 * </p>
 */
public interface AchievementAppService {

    /**
     * 获取成就列表
     *
     * @param page
     *            页码
     * @param size
     *            每页数量
     * @param type
     *            成就类型
     * @param awardLevel
     *            奖项级别
     * @param year
     *            年份
     * @return 分页成就结果
     */
    org.springframework.data.domain.Page<AchievementResult> getAchievements(Integer page, Integer size,
            com.bluenet.web.domain.model.enumerate.AchievementType type,
            com.bluenet.web.domain.model.enumerate.AwardLevel awardLevel, Integer year);

    /**
     * 获取成就统计
     *
     * @return 成就统计数据
     */
    AchievementStatistics getAchievementStats();

    /**
     * 创建成就
     *
     * @param command
     *            创建命令
     * @return 创建后的成就结果
     */
    AchievementResult createAchievement(AchievementCommands.CreateAchievementCommand command);

    /**
     * 更新成就
     *
     * @param command
     *            更新命令
     * @return 更新后的成就结果
     */
    AchievementResult updateAchievement(AchievementCommands.UpdateAchievementCommand command);

    /**
     * 删除成就
     *
     * @param id
     *            成就ID
     */
    void deleteAchievement(Long id);
}
