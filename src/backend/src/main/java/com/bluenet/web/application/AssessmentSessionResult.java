package com.bluenet.web.application;

import java.time.LocalDateTime;

/**
 * 考核会话聚合的应用层结果对象。
 * <p>
 * 封装了考核会话相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentSessionResult(
        /** 唯一标识 */
        Long id,
        /** 用户ID */
        Long userId,
        /** 考核时间ID */
        Long assessmentTimeId,
        /** 开始时间 */
        LocalDateTime startTime,
        /** 截止时间 */
        LocalDateTime deadline) {
}
