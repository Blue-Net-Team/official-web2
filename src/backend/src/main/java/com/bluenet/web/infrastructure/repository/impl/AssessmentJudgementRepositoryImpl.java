package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentJudgementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentJudgementRepositoryImpl implements AssessmentJudgementRepository {
    private final AssessmentJudgementMapper assessmentJudgementMapper;

    @Override
    public void save(AssessmentJudgement judgement) {
        log.info("save assessment judgement {}", judgement);
        assessmentJudgementMapper.insert(judgement);
    }

    @Override
    public Optional<AssessmentJudgementVO> findById(Long id) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectById(id);
        if (judgement == null) {
            log.warn("assessment judgement not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public void update(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = convertToEntity(judgement);
        int influence = assessmentJudgementMapper.updateById(entity);
        if (influence == 0) {
            log.warn("更新考核评判记录失败，judgementId {}", judgement.getId());
            throw new GlobalException("更新考核评判记录失败");
        }
    }

    @Override
    public Optional<AssessmentJudgementVO> findLatestByAnswerId(Long answerId) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectLatestByAnswerId(answerId);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public Optional<AssessmentJudgementVO> findLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        AssessmentJudgement judgement = assessmentJudgementMapper.selectLatestByQuestionIdAndUserId(questionId, userId);
        if (judgement == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(judgement));
    }

    @Override
    public List<AssessmentJudgementVO> findAllByQuestionId(Long questionId) {
        return assessmentJudgementMapper.selectAllByQuestionId(questionId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<AssessmentJudgementVO> findLatestObjectiveByQuestionId(Long questionId) {
        return assessmentJudgementMapper.selectLatestObjectiveByQuestionId(questionId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    private AssessmentJudgement convertToEntity(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = new AssessmentJudgement();
        entity.setId(judgement.getId());
        entity.setAnswerId(judgement.getAnswerId());
        entity.setQuestionId(judgement.getQuestionId());
        entity.setAssessmentTimeId(judgement.getAssessmentTimeId());
        entity.setUserId(judgement.getUserId());
        entity.setScore(judgement.getScore());
        entity.setMaxScore(judgement.getMaxScore());
        entity.setStatus(judgement.getStatus());
        entity.setResultCode(judgement.getResultCode());
        entity.setSource(judgement.getSource());
        entity.setReviewerId(judgement.getReviewerId());
        entity.setReviewerType(judgement.getReviewerType());
        entity.setComment(judgement.getComment());
        entity.setJudgedAt(judgement.getJudgedAt());
        entity.setCreatedAt(judgement.getCreatedAt());
        entity.setUpdatedAt(judgement.getUpdatedAt());
        return entity;
    }

    private AssessmentJudgementVO convertToVO(AssessmentJudgement judgement) {
        return AssessmentJudgementVO.builder()
                .id(judgement.getId())
                .answerId(judgement.getAnswerId())
                .questionId(judgement.getQuestionId())
                .assessmentTimeId(judgement.getAssessmentTimeId())
                .userId(judgement.getUserId())
                .score(judgement.getScore())
                .maxScore(judgement.getMaxScore())
                .status(judgement.getStatus())
                .resultCode(judgement.getResultCode())
                .source(judgement.getSource())
                .reviewerId(judgement.getReviewerId())
                .reviewerType(judgement.getReviewerType())
                .comment(judgement.getComment())
                .judgedAt(judgement.getJudgedAt())
                .createdAt(judgement.getCreatedAt())
                .updatedAt(judgement.getUpdatedAt())
                .build();
    }
}
