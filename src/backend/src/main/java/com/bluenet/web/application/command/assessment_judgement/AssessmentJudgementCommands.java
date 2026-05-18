package com.bluenet.web.application.command.assessment_judgement;

import java.math.BigDecimal;

/**
 * 考核评判聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AssessmentJudgementCommands {

    /** 禁止实例化。 */
    private AssessmentJudgementCommands() {
    }

    /**
     * 设置考核最终通过决策命令。
     * <p>
     * 用于设置考核的最终通过与否决策。
     * </p>
     */
    public record DecideAssessmentCommand(
            /** 用户ID */
            Long userId,
            /** 考核时间ID */
            Long assessmentTimeId,
            /** 是否通过 */
            Boolean passed,
            /** 决策评语 */
            String decisionComment) {
    }

    /**
     * 发布决策命令。
     * <p>
     * 用于发布考核评判决策结果。
     * </p>
     */
    public record PublishDecisionsCommand(
            /** 考核时间ID */
            Long assessmentTimeId) {
    }

    /**
     * 确认最终评分命令。
     * <p>
     * 方向管理员基于成员评论确认某题最终评分。
     * </p>
     */
    public record FinalizeScoreCommand(
            /** 答案ID */
            Long answerId,
            /** 最终分数 */
            BigDecimal score,
            /** 评语 */
            String comment) {
    }
}
