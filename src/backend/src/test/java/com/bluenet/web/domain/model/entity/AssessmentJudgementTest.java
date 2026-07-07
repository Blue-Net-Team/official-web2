package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentJudgement 领域实体单元测试。
 */
@DisplayName("AssessmentJudgement 领域实体测试")
class AssessmentJudgementTest {

    @Test
    @DisplayName("create: 应创建新评判记录")
    void create_shouldCreateJudgement() {
        LocalDateTime judgedAt = LocalDateTime.now();
        AssessmentJudgement judgement = AssessmentJudgement.create(
                1L,
                2L,
                3L,
                4L,
                new BigDecimal("80"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                5L,
                ReviewerType.SYSTEM,
                judgedAt);

        assertNull(judgement.getId());
        assertEquals(1L, judgement.getAnswerId());
        assertEquals(2L, judgement.getQuestionId());
        assertEquals(3L, judgement.getAssessmentTimeId());
        assertEquals(4L, judgement.getUserId());
        assertEquals(new BigDecimal("80"), judgement.getScore());
        assertEquals(JudgementSource.AUTO, judgement.getSource());
        assertEquals(ReviewerType.SYSTEM, judgement.getReviewerType());
        assertEquals(judgedAt, judgement.getJudgedAt());
    }

    @Test
    @DisplayName("applyJudgementResult: 应更新评分和状态字段")
    void applyJudgementResult_shouldUpdateFields() {
        AssessmentJudgement judgement = AssessmentJudgement.create(
                1L,
                2L,
                3L,
                4L,
                new BigDecimal("60"),
                new BigDecimal("100"),
                JudgementStatus.PENDING_MANUAL,
                null,
                JudgementSource.AUTO,
                null,
                null,
                LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        judgement.applyJudgementResult(
                new BigDecimal("90"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.ADMIN_FINALIZED,
                10L,
                ReviewerType.DIRECTION_ADMIN,
                now,
                now);

        assertEquals(new BigDecimal("90"), judgement.getScore());
        assertEquals(JudgementStatus.JUDGED, judgement.getStatus());
        assertEquals(ObjectiveResultCode.AC, judgement.getResultCode());
        assertEquals(JudgementSource.ADMIN_FINALIZED, judgement.getSource());
        assertEquals(10L, judgement.getReviewerId());
        assertEquals(ReviewerType.DIRECTION_ADMIN, judgement.getReviewerType());
        assertEquals(now, judgement.getJudgedAt());
        assertEquals(now, judgement.getUpdatedAt());
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0);
        LocalDateTime judgedAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        AssessmentJudgement judgement = AssessmentJudgement.reconstruct(
                100L,
                1L,
                2L,
                3L,
                4L,
                new BigDecimal("85"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.ADMIN_FINALIZED,
                10L,
                ReviewerType.SUPER_ADMIN,
                judgedAt,
                createdAt,
                updatedAt);

        assertEquals(100L, judgement.getId());
        assertEquals(createdAt, judgement.getCreatedAt());
        assertEquals(updatedAt, judgement.getUpdatedAt());
        assertEquals(judgedAt, judgement.getJudgedAt());
    }
}
