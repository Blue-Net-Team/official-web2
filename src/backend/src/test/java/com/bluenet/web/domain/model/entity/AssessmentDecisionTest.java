package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentDecision 领域实体单元测试。
 */
@DisplayName("AssessmentDecision 领域实体测试")
class AssessmentDecisionTest {

    @Test
    @DisplayName("create: 应创建未设置决策时间的新决策记录")
    void create_shouldCreatePendingDecision() {
        AssessmentDecision decision = AssessmentDecision.create(
                1L,
                2L,
                true,
                3L,
                "comment");

        assertNull(decision.getId());
        assertEquals(1L, decision.getUserId());
        assertEquals(2L, decision.getAssessmentTimeId());
        assertTrue(decision.getPassed());
        assertEquals(3L, decision.getDecidedBy());
        assertEquals("comment", decision.getDecisionComment());
        assertNull(decision.getDecidedAt());
        assertNull(decision.getUpdatedAt());
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime decidedAt = LocalDateTime.of(2024, 5, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 5, 1, 11, 0);

        AssessmentDecision decision = AssessmentDecision.reconstruct(
                10L,
                1L,
                2L,
                false,
                3L,
                "reconstructed",
                decidedAt,
                updatedAt);

        assertEquals(10L, decision.getId());
        assertEquals(1L, decision.getUserId());
        assertEquals(2L, decision.getAssessmentTimeId());
        assertFalse(decision.getPassed());
        assertEquals(3L, decision.getDecidedBy());
        assertEquals("reconstructed", decision.getDecisionComment());
        assertEquals(decidedAt, decision.getDecidedAt());
        assertEquals(updatedAt, decision.getUpdatedAt());
    }

    @Test
    @DisplayName("updatePassed: 应更新通过状态、决策人与说明，并设置决策时间")
    void updatePassed_shouldUpdateDecisionFields() {
        AssessmentDecision decision = AssessmentDecision.create(
                1L,
                2L,
                false,
                3L,
                "old");

        decision.updatePassed(true, 5L, "new comment");

        assertTrue(decision.getPassed());
        assertEquals(5L, decision.getDecidedBy());
        assertEquals("new comment", decision.getDecisionComment());
        assertNotNull(decision.getDecidedAt());
        assertNotNull(decision.getUpdatedAt());
        assertEquals(decision.getDecidedAt(), decision.getUpdatedAt());
    }

    @Test
    @DisplayName("updatePassed: passed 为 null 时应抛出 NullPointerException")
    void updatePassed_withNullPassed_shouldThrowNullPointerException() {
        AssessmentDecision decision = AssessmentDecision.create(
                1L,
                2L,
                true,
                3L,
                "comment");

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> decision.updatePassed(null, 5L, "new comment"));

        assertEquals("passed 不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("decideNow: 应同时设置决策时间和更新时间")
    void decideNow_shouldSetDecisionAndUpdateTime() {
        AssessmentDecision decision = AssessmentDecision.create(
                1L,
                2L,
                true,
                3L,
                "comment");
        assertNull(decision.getDecidedAt());
        assertNull(decision.getUpdatedAt());

        decision.decideNow();

        assertNotNull(decision.getDecidedAt());
        assertNotNull(decision.getUpdatedAt());
        assertEquals(decision.getDecidedAt(), decision.getUpdatedAt());
    }
}
