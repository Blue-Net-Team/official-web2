package com.bluenet.web.application.command.competition;

import com.bluenet.web.domain.model.enumerate.AwardLevel;

/**
 * 竞赛聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class CompetitionCommands {

    /** 禁止实例化。 */
    private CompetitionCommands() {
    }

    /**
     * 创建竞赛命令。
     * <p>
     * 用于创建新的竞赛信息。
     * </p>
     */
    public record CreateCompetitionCommand(
            /** 名称 */
            String name,
            /** 简称 */
            String shortName,
            /** Logo文件ID */
            Long logoFileId,
            /** 封面文件ID */
            Long coverFileId,
            /** 摘要 */
            String summary,
            /** 级别 */
            AwardLevel level,
            /** 月份 */
            String month,
            /** 主办方 */
            String organizer) {
    }

    /**
     * 更新竞赛命令。
     * <p>
     * 用于更新已有的竞赛信息。
     * </p>
     */
    public record UpdateCompetitionCommand(
            /** ID */
            Long id,
            /** 名称 */
            String name,
            /** 简称 */
            String shortName,
            /** Logo文件ID */
            Long logoFileId,
            /** 封面文件ID */
            Long coverFileId,
            /** 摘要 */
            String summary,
            /** 级别 */
            AwardLevel level,
            /** 月份 */
            String month,
            /** 主办方 */
            String organizer) {
    }

    /**
     * 更新排序命令。
     * <p>
     * 用于更新竞赛的排序顺序。
     * </p>
     */
    public record UpdateSortOrderCommand(
            /** ID */
            Long id,
            /** 排序顺序 */
            Integer sortOrder) {
    }

    /**
     * 移动竞赛命令。
     * <p>
     * 用于移动竞赛的位置方向。
     * </p>
     */
    public record MoveCompetitionCommand(
            /** ID */
            Long id,
            /** 方向 */
            String direction) {
    }

    /**
     * 批量排序项命令。
     * <p>
     * 用于批量排序竞赛的排序项。
     * </p>
     */
    public record SortItemCommand(
            /** ID */
            Long id,
            /** 排序顺序 */
            Integer sortOrder) {
    }
}
