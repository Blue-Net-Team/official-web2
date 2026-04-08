package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;

/**
 * 答案应用服务接口
 */
public interface AssessmentAnswerService {

    /**
     * 创建答案
     *
     * @param request
     *            创建请求DTO
     * @return 创建后的答案DTO
     */
    AssessmentAnswerDTO createAnswer(CreateAnswerRequestDTO request);

    /**
     * 更新答案（重新提交）
     *
     * @param request
     *            更新请求DTO
     * @return 更新后的答案DTO
     */
    AssessmentAnswerDTO updateAnswer(CreateAnswerRequestDTO request);

    /**
     * 查询当前用户对指定题目的答案
     *
     * @param questionId
     *            题目ID
     * @return 答案DTO，不存在返回null
     */
    AssessmentAnswerDTO getMyAnswer(Long questionId);
}
