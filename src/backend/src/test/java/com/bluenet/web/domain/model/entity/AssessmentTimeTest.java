package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AssessmentTime 领域实体单元测试。
 */
@DisplayName("AssessmentTime 领域实体测试")
class AssessmentTimeTest {

    @Test
    @DisplayName("create: 应创建新的考核时间聚合根")
    void create_shouldCreateAssessmentTime() {
        LocalDateTime startTime = TimeFixture.minusMinutes(5);
        LocalDateTime endTime = TimeFixture.plusMinutes(60);

        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                startTime,
                endTime,
                false,
                null,
                false);

        assertNull(assessmentTime.getId());
        assertEquals(Direction.COMPUTER_VISION, assessmentTime.getDirection());
        assertEquals(Integer.valueOf(1), assessmentTime.getEpoch());
        assertEquals(Integer.valueOf(2024), assessmentTime.getGrade());
        assertEquals(startTime, assessmentTime.getStartTime());
        assertEquals(endTime, assessmentTime.getEndTime());
        assertFalse(assessmentTime.getTimeLimit());
        assertNull(assessmentTime.getTimeLimitMinutes());
        assertFalse(assessmentTime.getAllowTeam());
        assertNull(assessmentTime.getResultsPublishedAt());
    }

    @Test
    @DisplayName("create: 开始时间不早于结束时间时应抛出异常")
    void create_shouldThrowWhenStartTimeNotBeforeEndTime() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 11, 10, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AssessmentTime.create(
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    now,
                    now,
                    false,
                    null,
                    false);
        });

        assertEquals("开始时间必须早于结束时间", exception.getMessage());
    }

    @Test
    @DisplayName("create: 限时考核未设置有效限时分钟数时应抛出异常")
    void create_shouldThrowWhenTimeLimitMinutesInvalid() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 11, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 11, 10, 0);

        IllegalArgumentException exceptionNullMinutes = assertThrows(IllegalArgumentException.class, () -> {
            AssessmentTime.create(
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    startTime,
                    endTime,
                    true,
                    null,
                    false);
        });
        assertEquals("限时考核必须设置有效的限时分钟数", exceptionNullMinutes.getMessage());

        IllegalArgumentException exceptionZeroMinutes = assertThrows(IllegalArgumentException.class, () -> {
            AssessmentTime.create(
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    startTime,
                    endTime,
                    true,
                    0,
                    false);
        });
        assertEquals("限时考核必须设置有效的限时分钟数", exceptionZeroMinutes.getMessage());
    }

    @Test
    @DisplayName("create: 非限时考核允许限时分钟数为 null")
    void create_shouldAllowNullTimeLimitMinutesWhenNotTimeLimited() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 11, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 11, 10, 0);

        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                startTime,
                endTime,
                false,
                null,
                false);

        assertFalse(assessmentTime.getTimeLimit());
        assertNull(assessmentTime.getTimeLimitMinutes());
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 11, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 7, 11, 10, 0);
        LocalDateTime resultsPublishedAt = LocalDateTime.of(2026, 7, 11, 12, 0);

        AssessmentTime assessmentTime = AssessmentTime.reconstruct(
                100L,
                Direction.STRUCTURAL_DESIGN,
                2,
                2023,
                startTime,
                endTime,
                true,
                90,
                resultsPublishedAt,
                true);

        assertEquals(Long.valueOf(100L), assessmentTime.getId());
        assertEquals(Direction.STRUCTURAL_DESIGN, assessmentTime.getDirection());
        assertEquals(Integer.valueOf(2), assessmentTime.getEpoch());
        assertEquals(Integer.valueOf(2023), assessmentTime.getGrade());
        assertEquals(startTime, assessmentTime.getStartTime());
        assertEquals(endTime, assessmentTime.getEndTime());
        assertTrue(assessmentTime.getTimeLimit());
        assertEquals(Integer.valueOf(90), assessmentTime.getTimeLimitMinutes());
        assertEquals(resultsPublishedAt, assessmentTime.getResultsPublishedAt());
        assertTrue(assessmentTime.getAllowTeam());
    }

    @Test
    @DisplayName("update: 应更新所有非 null 字段")
    void update_shouldUpdateNonNullFields() {
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder().build();

        LocalDateTime newStartTime = LocalDateTime.of(2026, 7, 11, 8, 0);
        LocalDateTime newEndTime = LocalDateTime.of(2026, 7, 11, 18, 0);

        assessmentTime.update(
                Direction.EMBEDDED,
                3,
                2025,
                newStartTime,
                newEndTime,
                true,
                120,
                true);

        assertEquals(Direction.EMBEDDED, assessmentTime.getDirection());
        assertEquals(Integer.valueOf(3), assessmentTime.getEpoch());
        assertEquals(Integer.valueOf(2025), assessmentTime.getGrade());
        assertEquals(newStartTime, assessmentTime.getStartTime());
        assertEquals(newEndTime, assessmentTime.getEndTime());
        assertTrue(assessmentTime.getTimeLimit());
        assertEquals(Integer.valueOf(120), assessmentTime.getTimeLimitMinutes());
        assertTrue(assessmentTime.getAllowTeam());
    }

    @Test
    @DisplayName("update: 应忽略所有 null 字段")
    void update_shouldIgnoreNullFields() {
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder().build();
        Direction originalDirection = assessmentTime.getDirection();
        Integer originalEpoch = assessmentTime.getEpoch();
        Integer originalGrade = assessmentTime.getGrade();
        LocalDateTime originalStartTime = assessmentTime.getStartTime();
        LocalDateTime originalEndTime = assessmentTime.getEndTime();
        Boolean originalTimeLimit = assessmentTime.getTimeLimit();
        Integer originalTimeLimitMinutes = assessmentTime.getTimeLimitMinutes();
        Boolean originalAllowTeam = assessmentTime.getAllowTeam();

        assessmentTime.update(null, null, null, null, null, null, null, null);

        assertEquals(originalDirection, assessmentTime.getDirection());
        assertEquals(originalEpoch, assessmentTime.getEpoch());
        assertEquals(originalGrade, assessmentTime.getGrade());
        assertEquals(originalStartTime, assessmentTime.getStartTime());
        assertEquals(originalEndTime, assessmentTime.getEndTime());
        assertEquals(originalTimeLimit, assessmentTime.getTimeLimit());
        assertEquals(originalTimeLimitMinutes, assessmentTime.getTimeLimitMinutes());
        assertEquals(originalAllowTeam, assessmentTime.getAllowTeam());
    }

    @Test
    @DisplayName("publishResults: 应设置结果发布时间")
    void publishResults_shouldSetResultsPublishedAt() {
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder().build();
        assertNull(assessmentTime.getResultsPublishedAt());

        assessmentTime.publishResults();

        assertNotNull(assessmentTime.getResultsPublishedAt());
    }

    @Test
    @DisplayName("isResultsPublished: 结果已发布时应返回 true")
    void isResultsPublished_shouldReturnTrueWhenPublished() {
        AssessmentTime assessmentTime = AssessmentTime.reconstruct(
                1L,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                false);

        assertTrue(assessmentTime.isResultsPublished());
    }

    @Test
    @DisplayName("isResultsPublished: 结果未发布时应返回 false")
    void isResultsPublished_shouldReturnFalseWhenNotPublished() {
        AssessmentTime assessmentTime = AssessmentFixture.timeBuilder().build();

        assertFalse(assessmentTime.isResultsPublished());
    }

    @Test
    @DisplayName("validateStartBeforeEnd: 开始时间早于结束时间时应通过")
    void validateStartBeforeEnd_shouldPassWhenStartBeforeEnd() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assessmentTime.validateStartBeforeEnd();
    }

    @Test
    @DisplayName("validateStartBeforeEnd: 开始时间不早于结束时间时应抛出异常")
    void validateStartBeforeEnd_shouldThrowWhenStartNotBeforeEnd() {
        AssessmentTime assessmentTime = AssessmentTime.reconstruct(
                1L,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 10, 0),
                LocalDateTime.of(2026, 7, 11, 9, 0),
                false,
                null,
                null,
                false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                assessmentTime::validateStartBeforeEnd);
        assertEquals("开始时间必须早于结束时间", exception.getMessage());
    }

    @Test
    @DisplayName("validateTimeLimit: 有效的限时设置时应通过")
    void validateTimeLimit_shouldPassWhenValid() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                true,
                60,
                false);

        assessmentTime.validateTimeLimit();
    }

    @Test
    @DisplayName("validateTimeLimit: 无效的限时设置时应抛出异常")
    void validateTimeLimit_shouldThrowWhenInvalid() {
        AssessmentTime assessmentTime = AssessmentTime.reconstruct(
                1L,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                true,
                null,
                null,
                false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                assessmentTime::validateTimeLimit);
        assertEquals("限时考核必须设置有效的限时分钟数", exception.getMessage());
    }

    @Test
    @DisplayName("hasStarted: 开始时间早于当前时间时应返回 true")
    void hasStarted_shouldReturnTrueWhenStartTimeBeforeNow() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                TimeFixture.minusMinutes(5),
                TimeFixture.plusMinutes(60),
                false,
                null,
                false);

        assertTrue(assessmentTime.hasStarted());
    }

    @Test
    @DisplayName("hasStarted: 开始时间晚于当前时间时应返回 false")
    void hasStarted_shouldReturnFalseWhenStartTimeAfterNow() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                TimeFixture.plusMinutes(10),
                TimeFixture.plusMinutes(70),
                false,
                null,
                false);

        assertFalse(assessmentTime.hasStarted());
    }

    @Test
    @DisplayName("hasStarted: 开始时间为 null 时应返回 false")
    void hasStarted_shouldReturnFalseWhenStartTimeIsNull() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                null,
                TimeFixture.plusMinutes(60),
                false,
                null,
                false);

        assertFalse(assessmentTime.hasStarted());
    }

    @Test
    @DisplayName("isGlobalFinalAssessment: 全局最终考核应返回 true")
    void isGlobalFinalAssessment_shouldReturnTrueForGlobalFinal() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                null,
                0,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertTrue(assessmentTime.isGlobalFinalAssessment());
    }

    @Test
    @DisplayName("isGlobalFinalAssessment: 方向考核应返回 false")
    void isGlobalFinalAssessment_shouldReturnFalseForDirectional() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                0,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertFalse(assessmentTime.isGlobalFinalAssessment());
    }

    @Test
    @DisplayName("isGlobalFinalAssessment: epoch 不为 0 时应返回 false")
    void isGlobalFinalAssessment_shouldReturnFalseWhenEpochNotZero() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                null,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertFalse(assessmentTime.isGlobalFinalAssessment());
    }

    @Test
    @DisplayName("getScope: 应返回包含方向和轮次的范围值对象")
    void getScope_shouldReturnScopeWithDirectionAndEpoch() {
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.EMBEDDED,
                2,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        AssessmentScope scope = assessmentTime.getScope();

        assertEquals(Direction.EMBEDDED, scope.direction());
        assertEquals(Integer.valueOf(2), scope.epoch());
    }

    @Test
    @DisplayName("matchesGrade: 两个考核年级相等时应返回 true")
    void matchesGrade_shouldReturnTrueWhenGradesEqual() {
        AssessmentTime timeOne = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);
        AssessmentTime timeTwo = AssessmentTime.create(
                Direction.STRUCTURAL_DESIGN,
                2,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertTrue(timeOne.matchesGrade(timeTwo));
    }

    @Test
    @DisplayName("matchesGrade: 任一考核年级为 null 时应返回 true")
    void matchesGrade_shouldReturnTrueWhenEitherGradeIsNull() {
        AssessmentTime timeWithGrade = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);
        AssessmentTime timeWithoutGrade = AssessmentTime.create(
                Direction.STRUCTURAL_DESIGN,
                2,
                null,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertTrue(timeWithGrade.matchesGrade(timeWithoutGrade));
        assertTrue(timeWithoutGrade.matchesGrade(timeWithGrade));
    }

    @Test
    @DisplayName("matchesGrade: 两个考核年级不同时应返回 false")
    void matchesGrade_shouldReturnFalseWhenGradesDiffer() {
        AssessmentTime timeOne = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);
        AssessmentTime timeTwo = AssessmentTime.create(
                Direction.STRUCTURAL_DESIGN,
                2,
                2025,
                LocalDateTime.of(2026, 7, 11, 9, 0),
                LocalDateTime.of(2026, 7, 11, 10, 0),
                false,
                null,
                false);

        assertFalse(timeOne.matchesGrade(timeTwo));
    }

    @Test
    @DisplayName("matchesGrade: 目标考核为 null 时应返回 false")
    void matchesGrade_shouldReturnFalseWhenOtherIsNull() {
        AssessmentTime timeOne = AssessmentFixture.timeBuilder().build();

        assertFalse(timeOne.matchesGrade(null));
    }
}
