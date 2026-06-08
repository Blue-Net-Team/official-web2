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
        AssessmentJudgement judgement = AssessmentJudgement.create(
                2L,
                3L,
                4L,
                5L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        judgement.setId(1L);
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
                judgedAt,
                updatedAt);

        assertEquals(new BigDecimal("88.50"), judgement.getScore());
        assertEquals(new BigDecimal("100"), judgement.getMaxScore());
        assertEquals(JudgementStatus.JUDGED, judgement.getStatus());
        assertEquals(ObjectiveResultCode.AC, judgement.getResultCode());
        assertEquals(JudgementSource.AUTO, judgement.getSource());
        assertEquals(10L, judgement.getReviewerId());
        assertEquals(ReviewerType.SYSTEM, judgement.getReviewerType());
        assertEquals(judgedAt, judgement.getJudgedAt());
        assertEquals(updatedAt, judgement.getUpdatedAt());
    }
}
