package com.bluenet.web.application;

import java.time.LocalDateTime;

/**
 * 考核决策聚合的应用层结果对象。
 * <p>
 * 封装了考核决策相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentDecisionResult(
        /** 唯一标识 */
        Long id,
        /** 用户ID */
        Long userId,
        /** 考核时间ID */
        Long assessmentTimeId,
        /** 是否通过 */
        Boolean passed,
        /** 决策人ID */
        Long decidedBy,
        /** 决策备注 */
        String decisionComment,
        /** 决策时间 */
        LocalDateTime decidedAt) {
}
