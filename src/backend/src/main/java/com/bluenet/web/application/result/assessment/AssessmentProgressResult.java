package com.bluenet.web.application.result.assessment;

/**
 * 考核进度应用层结果对象。
 */
public record AssessmentProgressResult(
        Long assessmentTimeId,
        Integer totalQuestions,
        Integer completedQuestions) {
}
