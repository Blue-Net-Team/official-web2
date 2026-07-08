package com.bluenet.web.application.service;

import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;

/**
 * 考核统计应用服务接口。
 * <p>
 * 定义了考核统计聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentStatisticsAppService {

    /**
     * 获取题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 题目统计结果
     */
    AssessmentStatisticsResult getQuestionStatistics(Long questionId);

    /**
     * 获取考生端题目统计信息。
     *
     * @param questionId
     *            题目ID
     * @return 考生可见的题目统计结果
     */
    AssessmentStatisticsResult getCandidateQuestionStatistics(Long questionId);
}
