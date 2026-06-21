package com.bluenet.web.infrastructure.repository.dataobject;

/**
 * 批量统计用户已完成答题数量的 MyBatis 结果对象。
 */
public record AssessmentAnswerCountResult(Long assessmentTimeId, Long count) {
}
