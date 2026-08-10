package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentJudgementDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentJudgementDomainServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class AssessmentJudgementDomainServiceImplTest {

    @Mock
    private AssessmentJudgementRepository assessmentJudgementRepository;

    private AssessmentJudgementDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new AssessmentJudgementDomainServiceImpl(assessmentJudgementRepository);
    }

    @Test
    @DisplayName("finalizeJudgement: 应执行 upsert 并查询返回 ADMIN_FINALIZED 记录")
    void finalizeJudgement_shouldUpsertAndReturnLatest() {
        AssessmentJudgement judgement = AssessmentJudgement.create(
                1L,
                2L,
                3L,
                4L,
                new BigDecimal("90"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                null,
                JudgementSource.ADMIN_FINALIZED,
                10L,
                ReviewerType.DIRECTION_ADMIN,
                null);

        AssessmentJudgement persisted = AssessmentJudgement.reconstruct(
                100L,
                judgement.getAnswerId(),
                judgement.getQuestionId(),
                judgement.getAssessmentTimeId(),
                judgement.getUserId(),
                judgement.getScore(),
                judgement.getMaxScore(),
                judgement.getStatus(),
                judgement.getResultCode(),
                judgement.getSource(),
                judgement.getReviewerId(),
                judgement.getReviewerType(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
        when(assessmentJudgementRepository.findLatestByAnswerIdAndSource(1L, JudgementSource.ADMIN_FINALIZED))
                .thenReturn(Optional.of(persisted));

        AssessmentJudgement result = domainService.finalizeJudgement(judgement);

        ArgumentCaptor<AssessmentJudgement> captor = ArgumentCaptor.forClass(AssessmentJudgement.class);
        verify(assessmentJudgementRepository).upsertAdminFinalized(captor.capture());
        AssessmentJudgement upserted = captor.getValue();
        assertEquals(1L, upserted.getAnswerId());
        assertEquals(JudgementSource.ADMIN_FINALIZED, upserted.getSource());
        assertNotNull(upserted.getCreatedAt());
        assertNotNull(upserted.getUpdatedAt());
        assertNotNull(upserted.getJudgedAt());

        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("finalizeJudgement: 当 judgedAt 已存在时不应覆盖原值")
    void finalizeJudgement_shouldPreserveExistingJudgedAt() {
        LocalDateTime existingJudgedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        AssessmentJudgement judgement = AssessmentJudgement.create(
                1L,
                2L,
                3L,
                4L,
                new BigDecimal("90"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                null,
                JudgementSource.ADMIN_FINALIZED,
                10L,
                ReviewerType.DIRECTION_ADMIN,
                existingJudgedAt);

        AssessmentJudgement persisted = AssessmentJudgement.reconstruct(
                100L,
                judgement.getAnswerId(),
                judgement.getQuestionId(),
                judgement.getAssessmentTimeId(),
                judgement.getUserId(),
                judgement.getScore(),
                judgement.getMaxScore(),
                judgement.getStatus(),
                judgement.getResultCode(),
                judgement.getSource(),
                judgement.getReviewerId(),
                judgement.getReviewerType(),
                existingJudgedAt,
                LocalDateTime.now(),
                LocalDateTime.now());
        when(assessmentJudgementRepository.findLatestByAnswerIdAndSource(1L, JudgementSource.ADMIN_FINALIZED))
                .thenReturn(Optional.of(persisted));

        domainService.finalizeJudgement(judgement);

        ArgumentCaptor<AssessmentJudgement> captor = ArgumentCaptor.forClass(AssessmentJudgement.class);
        verify(assessmentJudgementRepository).upsertAdminFinalized(captor.capture());
        AssessmentJudgement upserted = captor.getValue();
        assertEquals(existingJudgedAt, upserted.getJudgedAt());
    }
}
