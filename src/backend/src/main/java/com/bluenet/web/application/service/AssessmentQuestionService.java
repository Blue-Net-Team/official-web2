package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;

/**
 * 考题应用服务接口
 * <p>
 * 提供考题相关的应用层服务，协调领域服务完成业务操作
 * </p>
 */
public interface AssessmentQuestionService {

    /**
     * 创建考题
     *
     * @param request
     *            创建请求DTO
     * @return 创建后的考题DTO
     */
    AssessmentQuestionDTO createQuestion(CreateQuestionRequestDTO request);

    /**
     * 更新考题
     *
     * @param id
     *            考题ID
     * @param request
     *            更新请求DTO
     * @return 更新后的考题DTO
     */
    AssessmentQuestionDTO updateQuestion(Long id, UpdateQuestionRequestDTO request);

    /**
     * 删除考题
     *
     * @param id
     *            考题ID
     */
    void deleteQuestion(Long id);

    /**
     * 管理端分页查询考题
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 分页考题DTO
     */
    PageDTO<AssessmentQuestionDTO> listQuestionsForAdmin(Long assessmentTimeId, Integer page, Integer size);

    /**
     * 用户端分页查询考题目录（考生只能看到自己方向+年级的，不包含content）
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @return 用户考题列表响应（含分页考题和限时考核截止时间）
     */
    UserQuestionListResponse listQuestionsForUser(Long assessmentTimeId, Integer page, Integer size);

    /**
     * 用户端查询题目详情（包含content）
     *
     * @param id
     *            题目ID
     * @return 题目DTO
     */
    AssessmentQuestionDTO getQuestionDetailForUser(Long id);

    /**
     * 更新题目附件
     *
     * @param questionId
     *            题目ID
     * @param fileId
     *            文件ID（必须为 ASSESSMENT_ATTACHMENT 类型）
     */
    void updateAttachment(Long questionId, Long fileId);
}
