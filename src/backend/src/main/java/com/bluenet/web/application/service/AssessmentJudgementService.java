package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;

import java.util.List;

/**
 * 考核评判应用服务接口。
 */
public interface AssessmentJudgementService {

    /**
     * 查询答案最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判结果
     */
    AssessmentJudgementDTO getLatestByAnswerId(Long answerId);

    /**
     * 查询题目下的评判结果。
     *
     * @param questionId
     *            题目ID
     * @return 评判结果列表
     */
    List<AssessmentJudgementDTO> listByQuestionId(Long questionId);

    /**
     * 对文件上传题进行人工评分。
     *
     * @param request
     *            人工评分请求
     * @return 评判结果
     */
    AssessmentJudgementDTO reviewFileUploadAnswer(ManualReviewRequestDTO request);

    /**
     * 设置考生最终通过决策。
     *
     * @param request
     *            最终通过决策请求
     * @return 决策结果
     */
    AssessmentDecisionDTO decideAssessment(AssessmentDecisionRequestDTO request);
}
