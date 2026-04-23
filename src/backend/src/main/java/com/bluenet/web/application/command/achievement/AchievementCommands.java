package com.bluenet.web.application.command.achievement;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;

import java.time.LocalDate;

/**
 * 成就聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AchievementCommands {

    /** 禁止实例化。 */
    private AchievementCommands() {
    }

    /**
     * 创建成就命令。
     * <p>
     * 用于创建新的成就记录。
     * </p>
     */
    public record CreateAchievementCommand(
            /** 标题 */
            String title,
            /** 类型 */
            AchievementType type,
            /** 关联对象 */
            String relateTo,
            /** 达成日期 */
            LocalDate achieveAt,
            /** 奖项级别 */
            AwardLevel awardLevel,
            /** 奖项名称 */
            String awardName,
            /** 文件ID */
            Long fileId) {
    }

    /**
     * 更新成就命令。
     * <p>
     * 用于更新已有的成就记录。
     * </p>
     */
    public record UpdateAchievementCommand(
            /** ID */
            Long id,
            /** 标题 */
            String title,
            /** 类型 */
            AchievementType type,
            /** 关联对象 */
            String relateTo,
            /** 达成日期 */
            LocalDate achieveAt,
            /** 奖项级别 */
            AwardLevel awardLevel,
            /** 奖项名称 */
            String awardName,
            /** 文件ID */
            Long fileId) {
    }
}
