package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.Direction;

import java.time.LocalDateTime;

/**
 * 考核时间聚合的应用层结果对象。
 * <p>
 * 封装了考核时间相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentTimeResult(
        /** 唯一标识 */
        Long id,
        /** 方向 */
        Direction direction,
        /** 届数 */
        Integer epoch,
        /** 年级 */
        Integer grade,
        /** 开始时间 */
        LocalDateTime startTime,
        /** 结束时间 */
        LocalDateTime endTime,
        /** 是否限时 */
        Boolean timeLimit,
        /** 限时分钟数 */
        Integer timeLimitMinutes,
        /** 题目总数 */
        Integer totalQuestions,
        /** 已完成题目数 */
        Integer completedQuestions) {
}
