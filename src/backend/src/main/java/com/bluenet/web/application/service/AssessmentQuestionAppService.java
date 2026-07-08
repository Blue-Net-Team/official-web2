package com.bluenet.web.application.service;

import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.user.UserQuestionListResult;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import org.springframework.data.domain.Page;

/**
 * 考核题目应用服务接口。
 * <p>
 * 定义了考核题目聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentQuestionAppService {

    /**
     * 创建考核题目。
     *
     * @param command
     *            创建考核题目命令
     * @return 考核题目结果
     */
    AssessmentQuestionResult createQuestion(AssessmentQuestionCommands.CreateAssessmentQuestionCommand command);

    /**
     * 更新考核题目。
     *
     * @param command
     *            更新考核题目命令
     * @return 考核题目结果
     */
    AssessmentQuestionResult updateQuestion(AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command);

    /**
     * 删除考核题目。
     *
     * @param id
     *            题目ID
     */
    void deleteQuestion(Long id);

    /**
     * 管理端分页查询考核题目列表。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页数量
     * @return 分页考核题目结果
     */
    Page<AssessmentQuestionResult> listQuestionsForAdmin(Long assessmentTimeId, Integer page, Integer size);

    /**
     * 用户端查询考核题目列表。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页数量
     * @return 用户题目列表结果
     */
    UserQuestionListResult listQuestionsForUser(Long assessmentTimeId, Integer page, Integer size);

    /**
     * 用户端查询考核题目详情。
     *
     * @param id
     *            题目ID
     * @return 考核题目结果
     */
    AssessmentQuestionResult getQuestionDetailForUser(Long id);

    /**
     * 更新题目附件。
     *
     * @param questionId
     *            题目ID
     * @param fileId
     *            文件ID
     */
    void updateAttachment(Long questionId, Long fileId);
}
