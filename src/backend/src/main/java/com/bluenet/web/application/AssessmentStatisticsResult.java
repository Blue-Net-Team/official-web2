package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 考核统计聚合的应用层结果对象。
 * <p>
 * 封装了考核统计相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentStatisticsResult(
        /** 题目ID */
        Long questionId,
        /** 题目类型 */
        QuestionType questionType,
        /** 提交数量 */
        Long submittedCount,
        /** 通过数量 */
        Long acceptedCount,
        /** 通过率 */
        BigDecimal passRate,
        /** 结果分布 */
        Map<ObjectiveResultCode, Long> resultDistribution) {
}
