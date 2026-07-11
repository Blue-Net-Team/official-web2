package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AssessmentDecisionDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentDecisionDomainServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class AssessmentDecisionDomainServiceImplTest {

    @Mock
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    private AssessmentDecisionDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new AssessmentDecisionDomainServiceImpl(assessmentDecisionRepository, assessmentTimeRepository);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(userId): 用户无淘汰决策时应返回 false")
    void isEliminatedFromPriorEpochByUserId_noEliminatedDecisions_shouldReturnFalse() {
        Long userId = 1L;
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(userId))
                .thenReturn(Collections.emptyList());

        boolean result = domainService.isEliminatedFromPriorEpoch(userId, targetTime);

        assertFalse(result);
        verify(assessmentDecisionRepository).findEliminatedDecisionsByUserId(userId);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(userId): 用户存在先前轮次淘汰决策时应返回 true")
    void isEliminatedFromPriorEpochByUserId_withPriorElimination_shouldReturnTrue() {
        Long userId = 1L;
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        AssessmentDecision eliminatedDecision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();

        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(userId))
                .thenReturn(List.of(eliminatedDecision));
        when(assessmentTimeRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(priorTime));

        boolean result = domainService.isEliminatedFromPriorEpoch(userId, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(userId): 应查询所有决策关联的考核时间")
    void isEliminatedFromPriorEpochByUserId_shouldQueryDecisionTimes() {
        Long userId = 1L;
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        AssessmentDecision eliminatedDecision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();

        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(userId))
                .thenReturn(List.of(eliminatedDecision));
        when(assessmentTimeRepository.findAllById(List.of(10L)))
                .thenReturn(List.of(priorTime));

        domainService.isEliminatedFromPriorEpoch(userId, targetTime);

        verify(assessmentTimeRepository).findAllById(List.of(10L));
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 决策列表为空时应返回 false")
    void isEliminatedFromPriorEpochByDetails_emptyDecisions_shouldReturnFalse() {
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        boolean result = domainService.isEliminatedFromPriorEpoch(
                targetTime,
                Collections.emptyList(),
                Collections.emptyMap());

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 决策列表为 null 时应返回 false")
    void isEliminatedFromPriorEpochByDetails_nullDecisions_shouldReturnFalse() {
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, null, Collections.emptyMap());

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 缺少决策对应考核时间时应返回 false")
    void isEliminatedFromPriorEpochByDetails_missingTimeInMap_shouldReturnFalse() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();

        boolean result = domainService.isEliminatedFromPriorEpoch(
                targetTime,
                List.of(decision),
                Collections.emptyMap());

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 方向不同应返回 false")
    void isEliminatedFromPriorEpochByDetails_differentDirection_shouldReturnFalse() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.STRUCTURAL_DESIGN)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 年级不同应返回 false")
    void isEliminatedFromPriorEpochByDetails_differentGrade_shouldReturnFalse() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2023)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 先前轮次无效时应返回 false")
    void isEliminatedFromPriorEpochByDetails_invalidPriorEpoch_shouldReturnFalse() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(0)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 目标为最终轮次且先前轮次有效时应返回 true")
    void isEliminatedFromPriorEpochByDetails_targetFinalRound_shouldReturnTrue() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(0)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertTrue(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 同方向同年级且先前轮次小于目标轮次时应返回 true")
    void isEliminatedFromPriorEpochByDetails_priorEpochLessThanTarget_shouldReturnTrue() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertTrue(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 先前轮次等于目标轮次时应返回 false")
    void isEliminatedFromPriorEpochByDetails_priorEpochEqualsTarget_shouldReturnFalse() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);
        AssessmentDecision decision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(targetTime, List.of(decision), timeMap);

        assertFalse(result);
    }

    @Test
    @DisplayName("isEliminatedFromPriorEpoch(targetTime, decisions, timeMap): 多个决策中存在匹配时应返回 true")
    void isEliminatedFromPriorEpochByDetails_multipleDecisionsOneMatch_shouldReturnTrue() {
        AssessmentTime priorTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .grade(2024)
                .build();
        priorTime.setId(10L);
        AssessmentTime otherTime = AssessmentFixture.timeBuilder()
                .direction(Direction.EMBEDDED)
                .epoch(1)
                .grade(2024)
                .build();
        otherTime.setId(20L);
        AssessmentTime targetTime = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .epoch(2)
                .grade(2024)
                .build();
        targetTime.setId(100L);

        AssessmentDecision matchingDecision = AssessmentFixture.decisionBuilder()
                .assessmentTime(priorTime)
                .passed(false)
                .build();
        AssessmentDecision nonMatchingDecision = AssessmentFixture.decisionBuilder()
                .assessmentTime(otherTime)
                .passed(false)
                .build();

        Map<Long, AssessmentTime> timeMap = new HashMap<>();
        timeMap.put(10L, priorTime);
        timeMap.put(20L, otherTime);

        boolean result = domainService.isEliminatedFromPriorEpoch(
                targetTime,
                List.of(nonMatchingDecision, matchingDecision),
                timeMap);

        assertTrue(result);
    }
}
