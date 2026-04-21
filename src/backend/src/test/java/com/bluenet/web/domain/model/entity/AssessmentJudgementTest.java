package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssessmentJudgementTest {

    @Test
    void applyJudgementResult_shouldUpdateJudgementFields() {
        AssessmentJudgement judgement = AssessmentJudgement.builder()
                .id(1L)
                .answerId(2L)
                .questionId(3L)
                .assessmentTimeId(4L)
                .userId(5L)
                .build();
        LocalDateTime judgedAt = LocalDateTime.now().minusMinutes(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        judgement.applyJudgementResult(
                new BigDecimal("88.50"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                10L,
                ReviewerType.SYSTEM,
                "ok",
                judgedAt,
                updatedAt);

        assertEquals(new BigDecimal("88.50"), judgement.getScore());
        assertEquals(new BigDecimal("100"), judgement.getMaxScore());
        assertEquals(JudgementStatus.JUDGED, judgement.getStatus());
        assertEquals(ObjectiveResultCode.AC, judgement.getResultCode());
        assertEquals(JudgementSource.AUTO, judgement.getSource());
        assertEquals(10L, judgement.getReviewerId());
        assertEquals(ReviewerType.SYSTEM, judgement.getReviewerType());
        assertEquals("ok", judgement.getComment());
        assertEquals(judgedAt, judgement.getJudgedAt());
        assertEquals(updatedAt, judgement.getUpdatedAt());
    }
}
