package com.bluenet.web.application.command.learningpath;

import java.util.List;

/**
 * 学习路径聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class LearningPathCommands {

    /** 禁止实例化。 */
    private LearningPathCommands() {
    }

    /**
     * 创建学习步骤命令。
     * <p>
     * 用于创建新的学习步骤，顺序值由系统追加到方向末尾，不需要调用方提供。
     * </p>
     */
    public record CreateLearningStepCommand(
            /** 标识 */
            String slug,
            /** 标题 */
            String title,
            /** 相关链接URL */
            String relatedUrl) {
    }

    /**
     * 更新学习步骤命令。
     * <p>
     * 用于更新已有的学习步骤，不改变其展示顺序。
     * </p>
     */
    public record UpdateLearningStepCommand(
            /** ID */
            Long id,
            /** 标题 */
            String title,
            /** 相关链接URL */
            String relatedUrl) {
    }

    /**
     * 批量更新学习步骤排序命令。
     * <p>
     * 用于拖拽排序后全量覆盖某方向的步骤顺序。
     * </p>
     */
    public record BatchUpdateSortOrderCommand(
            /** 标识 */
            String slug,
            /** 排序项列表 */
            List<SortItemCommand> items) {
    }

    /**
     * 排序项命令
     */
    public record SortItemCommand(
            /** 步骤ID */
            Long id,
            /** 目标排序值 */
            Integer sortOrder) {
    }
}
