package com.bluenet.web.application.command.assessment_question;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.question_content.QuestionContent;

import java.math.BigDecimal;

/**
 * 考核题目聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AssessmentQuestionCommands {

    /** 禁止实例化。 */
    private AssessmentQuestionCommands() {
    }

    /**
     * 创建考核题目命令。
     * <p>
     * 用于创建新的考核题目。
     * </p>
     */
    public record CreateAssessmentQuestionCommand(
            /** 考核时间ID */
            Long assessmentTimeId,
            /** 题号 */
            Integer questionNo,
            /** 题目类型 */
            QuestionType questionType,
            /** 标题 */
            String title,
            /** 内容 */
            QuestionContent content,
            /** 附件ID */
            Long attachmentId,
            /** 分数 */
            BigDecimal score) {
    }

    /**
     * 克隆考核题目命令。
     *
     * @param sourceQuestionId
     *            源题目ID
     * @param targetAssessmentTimeId
     *            目标考核时间ID
     * @param targetQuestionNo
     *            目标题号
     */
    public record CloneAssessmentQuestionCommand(
            Long sourceQuestionId,
            Long targetAssessmentTimeId,
            Integer targetQuestionNo) {
    }

    /**
     * 更新考核题目命令。
     * <p>
     * 用于更新已有的考核题目。
     * </p>
     */
    public record UpdateAssessmentQuestionCommand(
            /** ID */
            Long id,
            /** 题号 */
            Integer questionNo,
            /** 题目类型 */
            QuestionType questionType,
            /** 标题 */
            String title,
            /** 内容 */
            QuestionContent content,
            /** 附件ID */
            Long attachmentId,
            /** 分数 */
            BigDecimal score) {
    }
}
