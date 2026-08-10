package com.bluenet.web.application.result.assessment;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考核评判聚合的应用层结果对象。
 * <p>
 * 封装了考核评判相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentJudgementResult(
        /** 唯一标识 */
        Long id,
        /** 答案ID */
        Long answerId,
        /** 题目ID */
        Long questionId,
        /** 考核时间ID */
        Long assessmentTimeId,
        /** 用户ID */
        Long userId,
        /** 得分 */
        BigDecimal score,
        /** 满分 */
        BigDecimal maxScore,
        /** 评判状态 */
        JudgementStatus status,
        /** 结果代码 */
        ObjectiveResultCode resultCode,
        /** 评判来源 */
        JudgementSource source,
        /** 评审人ID */
        Long reviewerId,
        /** 评审人类型 */
        ReviewerType reviewerType,
        /** 评判时间 */
        LocalDateTime judgedAt) {
}
