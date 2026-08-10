package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AssessmentScope 考核范围值对象单元测试。
 */
@DisplayName("AssessmentScope 考核范围值对象测试")
class AssessmentScopeTest {

    @Test
    @DisplayName("isGlobalFinal: direction 为 null 且 epoch 为 0 时应返回 true")
    void isGlobalFinal_shouldReturnTrueWhenDirectionNullAndEpochZero() {
        AssessmentScope scope = new AssessmentScope(null, 0);

        assertTrue(scope.isGlobalFinal());
    }

    @Test
    @DisplayName("isGlobalFinal: direction 非空时应返回 false")
    void isGlobalFinal_shouldReturnFalseWhenDirectionNotNull() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);

        assertFalse(scope.isGlobalFinal());
    }

    @Test
    @DisplayName("isGlobalFinal: epoch 不为 0 时应返回 false")
    void isGlobalFinal_shouldReturnFalseWhenEpochNotZero() {
        AssessmentScope scope = new AssessmentScope(null, 1);

        assertFalse(scope.isGlobalFinal());
    }

    @Test
    @DisplayName("isGlobalFinal: epoch 为 null 时应返回 false")
    void isGlobalFinal_shouldReturnFalseWhenEpochIsNull() {
        AssessmentScope scope = new AssessmentScope(null, null);

        assertFalse(scope.isGlobalFinal());
    }

    @Test
    @DisplayName("isDirectional: direction 非空时应返回 true")
    void isDirectional_shouldReturnTrueWhenDirectionNotNull() {
        AssessmentScope scope = new AssessmentScope(Direction.EMBEDDED, 1);

        assertTrue(scope.isDirectional());
    }

    @Test
    @DisplayName("isDirectional: direction 为 null 时应返回 false")
    void isDirectional_shouldReturnFalseWhenDirectionIsNull() {
        AssessmentScope scope = new AssessmentScope(null, 0);

        assertFalse(scope.isDirectional());
    }

    @Test
    @DisplayName("matches: 目标为全局最终考核且当前为有效的方向考核时应返回 true")
    void matches_shouldReturnTrueWhenTargetIsGlobalFinalAndCurrentIsValidDirectional() {
        AssessmentScope current = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope target = new AssessmentScope(null, 0);

        assertTrue(current.matches(target));
    }

    @Test
    @DisplayName("matches: 目标为全局最终考核但当前不是有效的方向考核时应返回 false")
    void matches_shouldReturnFalseWhenTargetIsGlobalFinalAndCurrentIsNotValidDirectional() {
        AssessmentScope currentGlobal = new AssessmentScope(null, 0);
        AssessmentScope currentInvalidEpoch = new AssessmentScope(Direction.COMPUTER_VISION, 0);
        AssessmentScope target = new AssessmentScope(null, 0);

        assertFalse(currentGlobal.matches(target));
        assertFalse(currentInvalidEpoch.matches(target));
    }

    @Test
    @DisplayName("matches: 目标为同方向方向考核时应返回 true")
    void matches_shouldReturnTrueWhenTargetIsSameDirectional() {
        AssessmentScope current = new AssessmentScope(Direction.STRUCTURAL_DESIGN, 2);
        AssessmentScope target = new AssessmentScope(Direction.STRUCTURAL_DESIGN, 1);

        assertTrue(current.matches(target));
    }

    @Test
    @DisplayName("matches: 目标为不同方向方向考核时应返回 false")
    void matches_shouldReturnFalseWhenTargetIsDifferentDirectional() {
        AssessmentScope current = new AssessmentScope(Direction.COMPUTER_VISION, 1);
        AssessmentScope target = new AssessmentScope(Direction.EMBEDDED, 1);

        assertFalse(current.matches(target));
    }

    @Test
    @DisplayName("matches: 目标为方向考核但当前不是方向考核时应返回 false")
    void matches_shouldReturnFalseWhenTargetIsDirectionalButCurrentIsNot() {
        AssessmentScope current = new AssessmentScope(null, 1);
        AssessmentScope target = new AssessmentScope(Direction.COMPUTER_VISION, 1);

        assertFalse(current.matches(target));
    }

    @Test
    @DisplayName("matches: 目标为 null 时应返回 false")
    void matches_shouldReturnFalseWhenTargetIsNull() {
        AssessmentScope current = new AssessmentScope(Direction.COMPUTER_VISION, 1);

        assertFalse(current.matches(null));
    }

    @Test
    @DisplayName("isFinalRound: epoch 为 0 时应返回 true")
    void isFinalRound_shouldReturnTrueWhenEpochIsZero() {
        AssessmentScope globalFinal = new AssessmentScope(null, 0);
        AssessmentScope directionalFinal = new AssessmentScope(Direction.COMPUTER_VISION, 0);

        assertTrue(globalFinal.isFinalRound());
        assertTrue(directionalFinal.isFinalRound());
    }

    @Test
    @DisplayName("isFinalRound: epoch 不为 0 时应返回 false")
    void isFinalRound_shouldReturnFalseWhenEpochIsNotZero() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 1);

        assertFalse(scope.isFinalRound());
    }

    @Test
    @DisplayName("isFinalRound: epoch 为 null 时应返回 false")
    void isFinalRound_shouldReturnFalseWhenEpochIsNull() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, null);

        assertFalse(scope.isFinalRound());
    }

    @Test
    @DisplayName("isValidDirectionalEpoch: epoch 为正整数时应返回 true")
    void isValidDirectionalEpoch_shouldReturnTrueWhenEpochPositive() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 1);

        assertTrue(scope.isValidDirectionalEpoch());
    }

    @Test
    @DisplayName("isValidDirectionalEpoch: epoch 为 0 时应返回 false")
    void isValidDirectionalEpoch_shouldReturnFalseWhenEpochIsZero() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, 0);

        assertFalse(scope.isValidDirectionalEpoch());
    }

    @Test
    @DisplayName("isValidDirectionalEpoch: epoch 为 null 时应返回 false")
    void isValidDirectionalEpoch_shouldReturnFalseWhenEpochIsNull() {
        AssessmentScope scope = new AssessmentScope(Direction.COMPUTER_VISION, null);

        assertFalse(scope.isValidDirectionalEpoch());
    }
}
