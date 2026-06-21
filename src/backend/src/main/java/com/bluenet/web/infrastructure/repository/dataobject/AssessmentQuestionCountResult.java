package com.bluenet.web.infrastructure.repository.dataobject;

/**
 * 批量统计考核题目数量的 MyBatis 结果对象。
 */
public record AssessmentQuestionCountResult(Long assessmentTimeId, Long count) {
}
