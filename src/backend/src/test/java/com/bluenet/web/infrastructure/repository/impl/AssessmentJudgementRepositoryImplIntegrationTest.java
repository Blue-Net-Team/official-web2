package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentJudgementMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentJudgementRepositoryImpl 集成测试。
 */
@DisplayName("AssessmentJudgementRepositoryImpl 集成测试")
class AssessmentJudgementRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentJudgementRepository assessmentJudgementRepository;

    @Autowired
    private AssessmentJudgementMapper assessmentJudgementMapper;

    private AssessmentJudgement createJudgement(Long answerId, Long questionId, Long assessmentTimeId, Long userId,
            BigDecimal score, JudgementSource source) {
        AssessmentJudgement judgement = AssessmentJudgement.create(
                answerId,
                questionId,
                assessmentTimeId,
                userId,
                score,
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                source,
                1L,
                com.bluenet.web.domain.model.enumerate.ReviewerType.SYSTEM,
                LocalDateTime.now());
        assessmentJudgementRepository.save(judgement);
        return judgement;
    }

    @Test
    @DisplayName("save: 应插入新记录并回写ID")
    void save_shouldInsertAndReturnId() {
        AssessmentJudgement judgement = createJudgement(1L, 2L, 3L, 4L, new BigDecimal("80"), JudgementSource.AUTO);

        assertNotNull(judgement.getId());
        AssessmentJudgementDO dataObject = assessmentJudgementMapper.selectById(judgement.getId());
        assertNotNull(dataObject);
        assertEquals(1L, dataObject.getAnswerId());
        assertEquals(JudgementSource.AUTO, dataObject.getSource());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        AssessmentJudgement judgement = createJudgement(1L, 2L, 3L, 4L, new BigDecimal("75"), JudgementSource.AUTO);

        Optional<AssessmentJudgement> found = assessmentJudgementRepository.findById(judgement.getId());
        assertTrue(found.isPresent());
        assertEquals(judgement.getAnswerId(), found.get().getAnswerId());

        assertTrue(assessmentJudgementRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findLatestByAnswerId: 应返回同一答案的最新记录")
    void findLatestByAnswerId_shouldReturnLatest() {
        createJudgement(10L, 2L, 3L, 4L, new BigDecimal("60"), JudgementSource.AUTO);
        AssessmentJudgement latest = createJudgement(10L, 2L, 3L, 4L, new BigDecimal("90"), JudgementSource.AUTO);

        Optional<AssessmentJudgement> found = assessmentJudgementRepository.findLatestByAnswerId(10L);

        assertTrue(found.isPresent());
        assertEquals(latest.getId(), found.get().getId());
        assertEquals(0, new BigDecimal("90").compareTo(found.get().getScore()));
    }

    @Test
    @DisplayName("findLatestByAnswerIdAndSource: 应按来源过滤")
    void findLatestByAnswerIdAndSource_shouldFilterBySource() {
        createJudgement(20L, 2L, 3L, 4L, new BigDecimal("70"), JudgementSource.AUTO);
        AssessmentJudgement adminFinalized = createJudgement(
                20L,
                2L,
                3L,
                4L,
                new BigDecimal("85"),
                JudgementSource.ADMIN_FINALIZED);

        Optional<AssessmentJudgement> found = assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(20L, JudgementSource.ADMIN_FINALIZED);

        assertTrue(found.isPresent());
        assertEquals(adminFinalized.getId(), found.get().getId());
    }

    @Test
    @DisplayName("findAllByQuestionId: 应返回题目下所有记录")
    void findAllByQuestionId_shouldReturnAll() {
        createJudgement(30L, 5L, 3L, 4L, new BigDecimal("80"), JudgementSource.AUTO);
        createJudgement(31L, 5L, 3L, 6L, new BigDecimal("90"), JudgementSource.AUTO);

        List<AssessmentJudgement> results = assessmentJudgementRepository.findAllByQuestionId(5L);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("findAnswerIdsBySource: 应返回有指定来源记录的 answerId 集合")
    void findAnswerIdsBySource_shouldReturnAnswerIds() {
        createJudgement(40L, 2L, 3L, 4L, new BigDecimal("80"), JudgementSource.AUTO);
        createJudgement(41L, 2L, 3L, 5L, new BigDecimal("85"), JudgementSource.ADMIN_FINALIZED);

        List<Long> answerIds = assessmentJudgementRepository
                .findAnswerIdsBySource(List.of(40L, 41L), JudgementSource.ADMIN_FINALIZED);

        assertEquals(List.of(41L), answerIds);
    }

    @Test
    @DisplayName("deleteByAnswerIds: 应删除指定答案的评审记录")
    void deleteByAnswerIds_shouldDeleteRecords() {
        AssessmentJudgement judgement = createJudgement(50L, 2L, 3L, 4L, new BigDecimal("80"), JudgementSource.AUTO);

        assessmentJudgementRepository.deleteByAnswerIds(List.of(50L));

        assertNull(assessmentJudgementMapper.selectById(judgement.getId()));
    }

    @Test
    @DisplayName("batchInsert: 应批量插入记录")
    void batchInsert_shouldInsertBatch() {
        AssessmentJudgement j1 = AssessmentJudgement.create(
                60L,
                2L,
                3L,
                4L,
                new BigDecimal("80"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                1L,
                com.bluenet.web.domain.model.enumerate.ReviewerType.SYSTEM,
                LocalDateTime.now());
        AssessmentJudgement j2 = AssessmentJudgement.create(
                61L,
                2L,
                3L,
                5L,
                new BigDecimal("90"),
                new BigDecimal("100"),
                JudgementStatus.JUDGED,
                ObjectiveResultCode.AC,
                JudgementSource.AUTO,
                1L,
                com.bluenet.web.domain.model.enumerate.ReviewerType.SYSTEM,
                LocalDateTime.now());

        assessmentJudgementRepository.batchInsert(List.of(j1, j2));

        assertNotNull(j1.getId());
        assertNotNull(j2.getId());
        assertNotNull(assessmentJudgementMapper.selectById(j1.getId()));
        assertNotNull(assessmentJudgementMapper.selectById(j2.getId()));
    }
}
