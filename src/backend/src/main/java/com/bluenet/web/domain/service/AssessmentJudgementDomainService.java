package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;

import java.util.List;

/**
 * 考核题目评判领域服务接口。
 */
public interface AssessmentJudgementDomainService {

    /**
     * 创建题目评判记录。
     *
     * @param judgement
     *            评判记录VO
     * @return 创建后的评判记录
     */
    AssessmentJudgementVO createJudgement(AssessmentJudgementVO judgement);

    /**
     * 更新题目评判记录。
     *
     * @param judgement
     *            评判记录VO
     * @return 更新后的评判记录
     */
    AssessmentJudgementVO updateJudgement(AssessmentJudgementVO judgement);

    /**
     * 查询评判记录详情。
     *
     * @param id
     *            评判记录ID
     * @return 评判记录
     */
    AssessmentJudgementVO getJudgementById(Long id);

    /**
     * 查询答案最新评判记录。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判记录
     */
    AssessmentJudgementVO getLatestByAnswerId(Long answerId);

    /**
     * 查询考生在某道题上的最新评判记录。
     *
     * @param questionId
     *            题目ID
     * @param userId
     *            考生用户ID
     * @return 最新评判记录
     */
    AssessmentJudgementVO getLatestByQuestionIdAndUserId(Long questionId, Long userId);

    /**
     * 查询一道题下的全部评判记录。
     *
     * @param questionId
     *            题目ID
     * @return 评判记录列表
     */
    List<AssessmentJudgementVO> listByQuestionId(Long questionId);
}
