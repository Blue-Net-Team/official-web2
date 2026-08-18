package com.bluenet.web.application.command.learningpath;

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
     * 用于创建新的学习步骤。
     * </p>
     */
    public record CreateLearningStepCommand(
            /** 标识 */
            String slug,
            /** 步骤编号 */
            Integer stepNumber,
            /** 标题 */
            String title,
            /** 相关链接URL */
            String relatedUrl) {
    }

    /**
     * 更新学习步骤命令。
     * <p>
     * 用于更新已有的学习步骤。
     * </p>
     */
    public record UpdateLearningStepCommand(
            /** ID */
            Long id,
            /** 步骤编号 */
            Integer stepNumber,
            /** 标题 */
            String title,
            /** 相关链接URL */
            String relatedUrl) {
    }
}
