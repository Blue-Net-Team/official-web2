package com.bluenet.web.application.command.assessment_answer;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;

/**
 * 考核答案聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AssessmentAnswerCommands {

    /** 禁止实例化。 */
    private AssessmentAnswerCommands() {
    }

    /**
     * 创建考核答案命令。
     * <p>
     * 用于创建新的考核答案记录。
     * </p>
     */
    public record CreateAssessmentAnswerCommand(
            /** 用户ID */
            Long userId,
            /** 题目ID */
            Long questionId,
            /** 内容 */
            String content,
            /** 编程语言 */
            ProgrammingLanguage language,
            /** 文件ID */
            Long fileId) {
    }

    /**
     * 更新考核答案命令。
     * <p>
     * 用于更新已有的考核答案记录。
     * </p>
     */
    public record UpdateAssessmentAnswerCommand(
            /** 用户ID */
            Long userId,
            /** 题目ID */
            Long questionId,
            /** 内容 */
            String content,
            /** 编程语言 */
            ProgrammingLanguage language,
            /** 文件ID */
            Long fileId) {
    }
}
