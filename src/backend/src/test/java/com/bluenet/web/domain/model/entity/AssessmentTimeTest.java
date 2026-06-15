package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssessmentTimeTest {

    @Test
    void isGlobalFinalAssessment_directionNullAndEpochZero_shouldReturnTrue() {
        AssessmentTime time = AssessmentTime.reconstruct(
                1L,
                null,
                0,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);

        assertTrue(time.isGlobalFinalAssessment());
    }

    @Test
    void isGlobalFinalAssessment_directionNotNull_shouldReturnFalse() {
        AssessmentTime time = AssessmentTime.reconstruct(
                1L,
                Direction.COMPUTER_VISION,
                2,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);

        assertFalse(time.isGlobalFinalAssessment());
    }

    @Test
    void isGlobalFinalAssessment_directionNullButEpochNotZero_shouldReturnFalse() {
        AssessmentTime time = AssessmentTime.reconstruct(
                1L,
                null,
                1,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);

        assertFalse(time.isGlobalFinalAssessment());
    }

    @Test
    void isGlobalFinalAssessment_epochNull_shouldReturnFalse() {
        AssessmentTime time = AssessmentTime.reconstruct(
                1L,
                null,
                null,
                2026,
                null,
                null,
                false,
                null,
                null,
                false);

        assertFalse(time.isGlobalFinalAssessment());
    }
}
