package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssessmentScopeTest {

    @Test
    void isGlobalFinal_directionNullAndEpochZero_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(null, 0);
        assertTrue(scope.isGlobalFinal());
    }

    @Test
    void isGlobalFinal_directionNotNull_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        assertFalse(scope.isGlobalFinal());
    }

    @Test
    void isGlobalFinal_epochNotZero_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(null, 1);
        assertFalse(scope.isGlobalFinal());
    }

    @Test
    void isGlobalFinal_epochNull_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(null, null);
        assertFalse(scope.isGlobalFinal());
    }

    @Test
    void isDirectional_directionNotNull_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        assertTrue(scope.isDirectional());
    }

    @Test
    void isDirectional_directionNull_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(null, 1);
        assertFalse(scope.isDirectional());
    }

    @Test
    void isDirectional_directionNotNullEpochZero_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        assertTrue(scope.isDirectional());
    }

    @Test
    void isDirectional_directionNotNullEpochNegative_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, -1);
        assertTrue(scope.isDirectional());
    }

    @Test
    void isDirectional_directionNotNullEpochNull_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, null);
        assertTrue(scope.isDirectional());
    }

    @Test
    void matches_targetGlobalFinalAndDecisionDirectional_shouldReturnTrue() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope targetScope = new AssessmentScope(null, 0);
        assertTrue(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetGlobalFinalAndDecisionDirectionalEpochZero_shouldReturnFalse() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        AssessmentScope targetScope = new AssessmentScope(null, 0);
        assertFalse(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetGlobalFinalAndDecisionGlobalFinal_shouldReturnFalse() {
        AssessmentScope decisionScope = new AssessmentScope(null, 0);
        AssessmentScope targetScope = new AssessmentScope(null, 0);
        assertFalse(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetDirectionalAndSameDirection_shouldReturnTrue() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope targetScope = new AssessmentScope(Direction.COMPUTER_VISION, 2);
        assertTrue(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetDirectionalAndDifferentDirection_shouldReturnFalse() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope targetScope = new AssessmentScope(Direction.EMBEDDED, 2);
        assertFalse(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetDirectionalAndDecisionGlobalFinal_shouldReturnFalse() {
        AssessmentScope decisionScope = new AssessmentScope(null, 0);
        AssessmentScope targetScope = new AssessmentScope(Direction.COMPUTER_VISION, 2);
        assertFalse(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetInvalidAndDecisionDirectional_shouldReturnFalse() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope targetScope = new AssessmentScope(null, 2);
        assertFalse(decisionScope.matches(targetScope));
    }

    @Test
    void matches_targetDirectionalEpochZeroAndSameDirection_shouldReturnTrue() {
        AssessmentScope decisionScope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope targetScope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        assertTrue(decisionScope.matches(targetScope));
    }

    @Test
    void isFinalRound_epochZero_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        assertTrue(scope.isFinalRound());
    }

    @Test
    void isFinalRound_epochPositive_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        assertFalse(scope.isFinalRound());
    }

    @Test
    void isFinalRound_epochNull_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, null);
        assertFalse(scope.isFinalRound());
    }

    @Test
    void isValidDirectionalEpoch_epochPositive_shouldReturnTrue() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        assertTrue(scope.isValidDirectionalEpoch());
    }

    @Test
    void isValidDirectionalEpoch_epochZero_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        assertFalse(scope.isValidDirectionalEpoch());
    }

    @Test
    void isValidDirectionalEpoch_epochNull_shouldReturnFalse() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, null);
        assertFalse(scope.isValidDirectionalEpoch());
    }
}
