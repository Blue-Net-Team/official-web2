package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentDecisionDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentDecisionDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentDecisionDomainServiceImplTest {

    private static final Long DECISION_ID = 200L;
    private static final Long USER_ID = 40L;
    private static final Long ASSESSMENT_TIME_ID = 30L;
    private static final Long DECIDED_BY = 1L;

    @Mock
    private AssessmentDecisionRepository assessmentDecisionRepository;

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @InjectMocks
    private AssessmentDecisionDomainServiceImpl assessmentDecisionDomainService;

    @Test
    @DisplayName("首次保存通过决策：应创建新记录并返回保存结果")
    void saveDecision_newDecision_shouldCreateAndReturnRecord() {
        AssessmentDecisionVO request = createDecisionVO(null, true);
        AssessmentDecisionVO saved = createDecisionVO(DECISION_ID, true);
        when(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(USER_ID, ASSESSMENT_TIME_ID))
                .thenReturn(Optional.empty());
        doAnswer(invocation -> {
            AssessmentDecision entity = invocation.getArgument(0);
            entity.setId(DECISION_ID);
            return null;
        }).when(assessmentDecisionRepository).save(any(AssessmentDecision.class));
        when(assessmentDecisionRepository.findById(DECISION_ID)).thenReturn(Optional.of(saved));

        AssessmentDecisionVO result = assessmentDecisionDomainService.saveDecision(request);

        assertEquals(DECISION_ID, result.getId());
        assertTrue(result.getPassed());
        ArgumentCaptor<AssessmentDecision> captor = ArgumentCaptor.forClass(AssessmentDecision.class);
        verify(assessmentDecisionRepository).save(captor.capture());
        // 首次决策由领域服务统一写入决策时间，避免由上层接口各自处理时间字段。
        assertNotNull(captor.getValue().getDecidedAt());
        assertNotNull(captor.getValue().getUpdatedAt());
        verify(assessmentDecisionRepository, never()).update(any());
    }

    @Test
    @DisplayName("重复保存通过决策：应覆盖同一考生同一考核时间的已有记录")
    void saveDecision_existingDecision_shouldUpdateExistingRecord() {
        AssessmentDecisionVO existing = createDecisionVO(DECISION_ID, false);
        AssessmentDecisionVO request = createDecisionVO(null, true);
        AssessmentDecisionVO updated = createDecisionVO(DECISION_ID, true);
        when(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(USER_ID, ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(existing));
        when(assessmentDecisionRepository.findById(DECISION_ID)).thenReturn(Optional.of(updated));

        AssessmentDecisionVO result = assessmentDecisionDomainService.saveDecision(request);

        assertTrue(result.getPassed());
        ArgumentCaptor<AssessmentDecisionVO> captor = ArgumentCaptor.forClass(AssessmentDecisionVO.class);
        verify(assessmentDecisionRepository).update(captor.capture());
        assertEquals(DECISION_ID, captor.getValue().getId());
        assertTrue(captor.getValue().getPassed());
        assertNotNull(captor.getValue().getDecidedAt());
        assertNotNull(captor.getValue().getUpdatedAt());
        verify(assessmentDecisionRepository, never()).save(any());
    }

    @Test
    @DisplayName("首次保存后无法回查：应抛出全局异常")
    void saveDecision_missingAfterCreate_shouldThrowGlobalException() {
        AssessmentDecisionVO request = createDecisionVO(null, true);
        when(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(USER_ID, ASSESSMENT_TIME_ID))
                .thenReturn(Optional.empty());
        doAnswer(invocation -> {
            AssessmentDecision entity = invocation.getArgument(0);
            entity.setId(DECISION_ID);
            return null;
        }).when(assessmentDecisionRepository).save(any(AssessmentDecision.class));
        when(assessmentDecisionRepository.findById(DECISION_ID)).thenReturn(Optional.empty());

        assertThrows(GlobalException.class, () -> assessmentDecisionDomainService.saveDecision(request));
    }

    @Test
    @DisplayName("按ID查询不存在的通过决策：应抛出DataNotFound")
    void getDecisionById_missing_shouldThrowDataNotFound() {
        when(assessmentDecisionRepository.findById(DECISION_ID)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> assessmentDecisionDomainService.getDecisionById(DECISION_ID));
    }

    @Test
    @DisplayName("按考生和考核时间查询通过决策：存在时应返回记录")
    void getDecision_existing_shouldReturnRecord() {
        AssessmentDecisionVO decision = createDecisionVO(DECISION_ID, true);
        when(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(USER_ID, ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(decision));

        AssessmentDecisionVO result = assessmentDecisionDomainService.getDecision(USER_ID, ASSESSMENT_TIME_ID);

        assertEquals(DECISION_ID, result.getId());
        assertTrue(result.getPassed());
    }

    @Test
    @DisplayName("按考生和考核时间查询不存在的通过决策：应抛出DataNotFound")
    void getDecision_missing_shouldThrowDataNotFound() {
        when(assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(USER_ID, ASSESSMENT_TIME_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> assessmentDecisionDomainService.getDecision(USER_ID, ASSESSMENT_TIME_ID));
    }

    private AssessmentDecisionVO createDecisionVO(Long id, boolean passed) {
        return AssessmentDecisionVO.builder()
                .id(id)
                .userId(USER_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .passed(passed)
                .decidedBy(DECIDED_BY)
                .decisionComment("人工最终决策")
                .build();
    }

    // ==================== isEliminatedFromPriorEpoch 测试 ====================

    @Test
    @DisplayName("无淘汰决策：应返回false")
    void isEliminatedFromPriorEpoch_noEliminatedDecisions_shouldReturnFalse() {
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        AssessmentTime targetTime = createAssessmentTime(2, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰，查看epoch2，同方向同年级：应返回true")
    void isEliminatedFromPriorEpoch_eliminatedInEpoch1_targetEpoch2_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(2, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰，查看epoch1，同方向同年级：应返回false（同一轮次）")
    void isEliminatedFromPriorEpoch_eliminatedInEpoch1_targetEpoch1_shouldReturnFalse() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(1, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰，查看epoch0（最终考核），同方向同年级：应返回true")
    void isEliminatedFromPriorEpoch_eliminatedInEpoch1_targetFinal_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(0, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1方向考核被淘汰，查看全局考核epoch=0且direction=null：应返回true")
    void isEliminatedFromPriorEpoch_directionEliminated_targetGlobal_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(0, null, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1方向考核被淘汰，查看全局考核direction=null且grade=null：应返回true")
    void isEliminatedFromPriorEpoch_directionEliminated_targetGlobalNullGrade_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(0, null, null);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1方向考核被淘汰，查看全局考核direction=null但不同grade：应返回false")
    void isEliminatedFromPriorEpoch_directionEliminated_targetGlobalDifferentGrade_shouldReturnFalse() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(0, null, 2023);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰（grade=null），查看epoch2同方向但grade=2024：应返回true（不限年级的淘汰限制所有年级）")
    void isEliminatedFromPriorEpoch_eliminatedNullGrade_targetDifferentGrade_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, null)));

        AssessmentTime targetTime = createAssessmentTime(2, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰（grade=null），查看全局考核grade=2024：应返回true（不限年级的淘汰限制所有年级）")
    void isEliminatedFromPriorEpoch_eliminatedNullGrade_targetGlobalWithGrade_shouldReturnTrue() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, null)));

        AssessmentTime targetTime = createAssessmentTime(0, null, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("在epoch1被淘汰，查看epoch2，不同方向：应返回false")
    void isEliminatedFromPriorEpoch_differentDirection_shouldReturnFalse() {
        AssessmentDecisionVO decision = createDecisionVO(1L, false);
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(List.of(decision));
        when(assessmentTimeRepository.findById(ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(createAssessmentTime(1, Direction.COMPUTER_VISION, 2024)));

        AssessmentTime targetTime = createAssessmentTime(2, Direction.EMBEDDED, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("决策改为通过后：应返回false（限制立即解除）")
    void isEliminatedFromPriorEpoch_passDecisionReversed_shouldReturnFalse() {
        when(assessmentDecisionRepository.findEliminatedDecisionsByUserId(USER_ID))
                .thenReturn(Collections.emptyList());

        AssessmentTime targetTime = createAssessmentTime(2, Direction.COMPUTER_VISION, 2024);
        boolean result = assessmentDecisionDomainService.isEliminatedFromPriorEpoch(USER_ID, targetTime);

        assertFalse(result);
    }

    private AssessmentTime createAssessmentTime(int epoch, Direction direction, Integer grade) {
        return AssessmentTime.reconstruct(
                ASSESSMENT_TIME_ID,
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
