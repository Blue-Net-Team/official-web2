package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
