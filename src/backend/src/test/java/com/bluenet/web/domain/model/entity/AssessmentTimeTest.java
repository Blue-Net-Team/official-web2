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

    @Test
    void getScope_globalFinal_shouldReturnGlobalFinalScope() {
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

        AssessmentScope scope = time.getScope();
        assertTrue(scope.isGlobalFinal());
        assertFalse(scope.isDirectional());
    }

    @Test
    void getScope_directional_shouldReturnDirectionalScope() {
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

        AssessmentScope scope = time.getScope();
        assertFalse(scope.isGlobalFinal());
        assertTrue(scope.isDirectional());
        assertTrue(scope.matches(new AssessmentScope(Direction.COMPUTER_VISION, 3)));
    }

    @Test
    void matchesGrade_bothNull_shouldReturnTrue() {
        AssessmentTime eliminatedTime = createAssessmentTime(Direction.COMPUTER_VISION, 1, null);
        AssessmentTime targetTime = createAssessmentTime(Direction.COMPUTER_VISION, 2, null);
        assertTrue(eliminatedTime.matchesGrade(targetTime));
    }

    @Test
    void matchesGrade_eliminatedNull_shouldReturnTrue() {
        AssessmentTime eliminatedTime = createAssessmentTime(Direction.COMPUTER_VISION, 1, null);
        AssessmentTime targetTime = createAssessmentTime(Direction.COMPUTER_VISION, 2, 2024);
        assertTrue(eliminatedTime.matchesGrade(targetTime));
    }

    @Test
    void matchesGrade_targetNull_shouldReturnTrue() {
        AssessmentTime eliminatedTime = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        AssessmentTime targetTime = createAssessmentTime(Direction.COMPUTER_VISION, 2, null);
        assertTrue(eliminatedTime.matchesGrade(targetTime));
    }

    @Test
    void matchesGrade_sameGrade_shouldReturnTrue() {
        AssessmentTime eliminatedTime = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        AssessmentTime targetTime = createAssessmentTime(Direction.COMPUTER_VISION, 2, 2024);
        assertTrue(eliminatedTime.matchesGrade(targetTime));
    }

    @Test
    void matchesGrade_differentGrade_shouldReturnFalse() {
        AssessmentTime eliminatedTime = createAssessmentTime(Direction.COMPUTER_VISION, 1, 2024);
        AssessmentTime targetTime = createAssessmentTime(Direction.COMPUTER_VISION, 2, 2023);
        assertFalse(eliminatedTime.matchesGrade(targetTime));
    }

    private AssessmentTime createAssessmentTime(Direction direction, int epoch, Integer grade) {
        return AssessmentTime.reconstruct(
                1L,
                direction,
                epoch,
                grade,
                null,
                null,
                false,
                null,
                null,
                false);
    }
}
