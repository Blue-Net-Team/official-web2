package com.bluenet.web.application.service;

import com.bluenet.web.application.result.assessment.AssessmentAnswerResult;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;

/**
 * 考核答案应用服务接口。
 * <p>
 * 定义了考核答案聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentAnswerAppService {

    /**
     * 创建考核答案。
     *
     * @param command
     *            创建考核答案命令
     * @return 考核答案结果
     */
    AssessmentAnswerResult createAnswer(AssessmentAnswerCommands.CreateAssessmentAnswerCommand command);

    /**
     * 更新考核答案。
     *
     * @param command
     *            更新考核答案命令
     * @return 考核答案结果
     */
    AssessmentAnswerResult updateAnswer(AssessmentAnswerCommands.UpdateAssessmentAnswerCommand command);

    /**
     * 查询当前用户的考核答案。
     *
     * @param userId
     *            用户ID
     * @param questionId
     *            题目ID
     * @return 考核答案结果
     */
    AssessmentAnswerResult getMyAnswer(Long userId, Long questionId);
}
