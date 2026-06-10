package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentJudgementDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentJudgementDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentJudgementDomainServiceImplTest {

    private static final Long JUDGEMENT_ID = 100L;
    private static final Long ANSWER_ID = 10L;
    private static final Long QUESTION_ID = 20L;
    private static final Long ASSESSMENT_TIME_ID = 30L;
    private static final Long USER_ID = 40L;

    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @InjectMocks
    private AssessmentJudgementDomainServiceImpl assessmentJudgementDomainService;

    @Test
    @DisplayName("创建评判记录：应保存时间戳并返回创建后的记录")
    void createJudgement_valid_shouldSaveAndReturnCreatedRecord() {
        AssessmentJudgementVO request = createJudgementVO(null, ObjectiveResultCode.AC);
        AssessmentJudgement savedEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        doAnswer(invocation -> {
            AssessmentJudgement entity = invocation.getArgument(0);
            entity.setId(JUDGEMENT_ID);
            return null;
        }).when(assessmentJudgementRepository).save(any(AssessmentJudgement.class));
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID)).thenReturn(Optional.of(savedEntity));

        AssessmentJudgementVO result = assessmentJudgementDomainService.createJudgement(request);

        assertEquals(JUDGEMENT_ID, result.getId());
        assertEquals(ObjectiveResultCode.AC, result.getResultCode());
        ArgumentCaptor<AssessmentJudgement> captor = ArgumentCaptor.forClass(AssessmentJudgement.class);
        verify(assessmentJudgementRepository).save(captor.capture());
        // 创建时由领域服务补齐评判时间，便于同步自动评判和人工评判统一落库。
        assertNotNull(captor.getValue().getJudgedAt());
        assertNotNull(captor.getValue().getCreatedAt());
        assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    @DisplayName("创建评判记录后无法回查：应抛出全局异常")
    void createJudgement_missingAfterSave_shouldThrowGlobalException() {
        AssessmentJudgementVO request = createJudgementVO(null, ObjectiveResultCode.WA);
        doAnswer(invocation -> {
            AssessmentJudgement entity = invocation.getArgument(0);
            entity.setId(JUDGEMENT_ID);
            return null;
        }).when(assessmentJudgementRepository).save(any(AssessmentJudgement.class));
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID)).thenReturn(Optional.empty());

        assertThrows(GlobalException.class, () -> assessmentJudgementDomainService.createJudgement(request));
    }

    @Test
    @DisplayName("更新评判记录：应先校验存在并返回更新后的记录")
    void updateJudgement_existing_shouldUpdateAndReturnLatestRecord() {
        AssessmentJudgementVO update = createJudgementVO(JUDGEMENT_ID, ObjectiveResultCode.AC);
        AssessmentJudgement existingEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.WA);
        AssessmentJudgement updatedEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID))
                .thenReturn(Optional.of(existingEntity))
                .thenReturn(Optional.of(updatedEntity));

        AssessmentJudgementVO result = assessmentJudgementDomainService.updateJudgement(update);

        assertEquals(ObjectiveResultCode.AC, result.getResultCode());
        assertNotNull(update.getUpdatedAt());
        verify(assessmentJudgementRepository).update(any(AssessmentJudgement.class));
    }

    @Test
    @DisplayName("更新评判记录缺少ID：应抛出全局异常")
    void updateJudgement_missingId_shouldThrowGlobalException() {
        AssessmentJudgementVO update = createJudgementVO(null, ObjectiveResultCode.AC);

        assertThrows(GlobalException.class, () -> assessmentJudgementDomainService.updateJudgement(update));
        verify(assessmentJudgementRepository, never()).update(any());
    }

    @Test
    @DisplayName("查询不存在的评判记录：应抛出DataNotFound")
    void getJudgementById_missing_shouldThrowDataNotFound() {
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> assessmentJudgementDomainService.getJudgementById(JUDGEMENT_ID));
    }

    @Test
    @DisplayName("按答案查询最新评判记录：存在时应返回记录")
    void getLatestByAnswerId_existing_shouldReturnRecord() {
        AssessmentJudgement judgementEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.WA);
        when(assessmentJudgementRepository.findLatestByAnswerId(ANSWER_ID)).thenReturn(Optional.of(judgementEntity));

        AssessmentJudgementVO result = assessmentJudgementDomainService.getLatestByAnswerId(ANSWER_ID);

        assertEquals(JUDGEMENT_ID, result.getId());
        verify(assessmentJudgementRepository).findLatestByAnswerId(ANSWER_ID);
    }

    @Test
    @DisplayName("按题目查询评判记录列表：应透传仓储结果")
    void listByQuestionId_shouldReturnRepositoryRecords() {
        AssessmentJudgement judgementEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        when(assessmentJudgementRepository.findAllByQuestionId(QUESTION_ID)).thenReturn(List.of(judgementEntity));

        List<AssessmentJudgementVO> result = assessmentJudgementDomainService.listByQuestionId(QUESTION_ID);

        assertEquals(1, result.size());
        assertEquals(JUDGEMENT_ID, result.get(0).getId());
    }

    @Test
    @DisplayName("确认最终评分：应保存ADMIN_FINALIZED记录并返回")
    void finalizeJudgement_valid_shouldSaveAndReturnCreatedRecord() {
        AssessmentJudgementVO request = createJudgementVO(null, ObjectiveResultCode.AC);
        request.setSource(JudgementSource.ADMIN_FINALIZED);
        AssessmentJudgement savedEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        savedEntity.setSource(JudgementSource.ADMIN_FINALIZED);
        doAnswer(invocation -> {
            AssessmentJudgement entity = invocation.getArgument(0);
            entity.setId(JUDGEMENT_ID);
            return null;
        }).when(assessmentJudgementRepository).save(any(AssessmentJudgement.class));
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID)).thenReturn(Optional.of(savedEntity));

        AssessmentJudgementVO result = assessmentJudgementDomainService.finalizeJudgement(request);

        assertEquals(JUDGEMENT_ID, result.getId());
        assertEquals(JudgementSource.ADMIN_FINALIZED, result.getSource());
        ArgumentCaptor<AssessmentJudgement> captor = ArgumentCaptor.forClass(AssessmentJudgement.class);
        verify(assessmentJudgementRepository).save(captor.capture());
        assertNotNull(captor.getValue().getJudgedAt());
        assertNotNull(captor.getValue().getCreatedAt());
        assertNotNull(captor.getValue().getUpdatedAt());
    }

    @Test
    @DisplayName("确认最终评分：同一答案多次finalize应覆盖更新")
    void finalizeJudgement_existingAdminFinalized_shouldUpdateInsteadOfInsert() {
        AssessmentJudgementVO request = createJudgementVO(null, ObjectiveResultCode.AC);
        request.setSource(JudgementSource.ADMIN_FINALIZED);
        request.setScore(BigDecimal.valueOf(95));

        AssessmentJudgement existingEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        existingEntity.setSource(JudgementSource.ADMIN_FINALIZED);
        existingEntity.setScore(BigDecimal.valueOf(80));

        AssessmentJudgement updatedEntity = createJudgementEntity(JUDGEMENT_ID, ObjectiveResultCode.AC);
        updatedEntity.setSource(JudgementSource.ADMIN_FINALIZED);
        updatedEntity.setScore(BigDecimal.valueOf(95));

        when(assessmentJudgementRepository.findLatestByAnswerIdAndSource(ANSWER_ID, JudgementSource.ADMIN_FINALIZED))
                .thenReturn(Optional.of(existingEntity));
        when(assessmentJudgementRepository.findById(JUDGEMENT_ID)).thenReturn(Optional.of(updatedEntity));

        AssessmentJudgementVO result = assessmentJudgementDomainService.finalizeJudgement(request);

        assertEquals(JUDGEMENT_ID, result.getId());
        assertEquals(BigDecimal.valueOf(95), result.getScore());
        assertEquals(JudgementSource.ADMIN_FINALIZED, result.getSource());
        verify(assessmentJudgementRepository, never()).save(any());
        verify(assessmentJudgementRepository).update(any(AssessmentJudgement.class));
    }

    private AssessmentJudgementVO createJudgementVO(Long id, ObjectiveResultCode resultCode) {
        return AssessmentJudgementVO.builder()
                .id(id)
                .answerId(ANSWER_ID)
                .questionId(QUESTION_ID)
                .assessmentTimeId(ASSESSMENT_TIME_ID)
                .userId(USER_ID)
                .score(resultCode == ObjectiveResultCode.AC ? BigDecimal.TEN : BigDecimal.ZERO)
                .maxScore(BigDecimal.TEN)
                .status(JudgementStatus.JUDGED)
                .resultCode(resultCode)
                .source(JudgementSource.AUTO)
                .build();
    }

    private AssessmentJudgement createJudgementEntity(Long id, ObjectiveResultCode resultCode) {
        return AssessmentJudgement.reconstruct(
                id,
                ANSWER_ID,
                QUESTION_ID,
                ASSESSMENT_TIME_ID,
                USER_ID,
                resultCode == ObjectiveResultCode.AC ? BigDecimal.TEN : BigDecimal.ZERO,
                BigDecimal.TEN,
                JudgementStatus.JUDGED,
                resultCode,
                JudgementSource.AUTO,
                null,
                null,
                null,
                null,
                null);
    }
}
