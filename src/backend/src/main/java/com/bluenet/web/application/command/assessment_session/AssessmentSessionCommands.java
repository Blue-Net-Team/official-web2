package com.bluenet.web.application.command.assessment_session;

/**
 * 考核会话聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AssessmentSessionCommands {

    /** 禁止实例化。 */
    private AssessmentSessionCommands() {
    }

    /**
     * 获取或创建考核会话命令。
     * <p>
     * 用于获取或创建用户的考核会话。
     * </p>
     */
    public record GetOrCreateSessionCommand(
            /** 用户ID */
            Long userId,
            /** 考核时间ID */
            Long assessmentTimeId) {
    }
}
