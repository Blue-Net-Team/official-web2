package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentJudgementDomainServiceImpl implements AssessmentJudgementDomainService {
    private final AssessmentJudgementRepository assessmentJudgementRepository;

    @Override
    @Transactional
    public AssessmentJudgementVO createJudgement(AssessmentJudgementVO judgement) {
        log.info(
                "create assessment judgement for answer {}, question {}",
                judgement.getAnswerId(),
                judgement.getQuestionId());

        AssessmentJudgement entity = convertToEntity(judgement);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        // 没有显式评判完成时间时，用当前时间作为同步/人工评判的完成时间。
        if (entity.getJudgedAt() == null) {
            entity.setJudgedAt(now);
        }
        assessmentJudgementRepository.save(entity);

        return convertToVO(
                assessmentJudgementRepository.findById(entity.getId())
                        .orElseThrow(() -> new GlobalException("创建考核评判记录失败")));
    }

    @Override
    @Transactional
    public AssessmentJudgementVO updateJudgement(AssessmentJudgementVO judgement) {
        log.info("update assessment judgement id {}", judgement.getId());
        if (judgement.getId() == null) {
            throw new GlobalException("更新考核评判记录失败：评判ID不能为空");
        }
        getJudgementById(judgement.getId());
        judgement.setUpdatedAt(LocalDateTime.now());
        assessmentJudgementRepository.update(convertToEntity(judgement));
        return convertToVO(
                assessmentJudgementRepository.findById(judgement.getId())
                        .orElseThrow(() -> new GlobalException("更新考核评判记录失败")));
    }

    @Override
    public AssessmentJudgementVO getJudgementById(Long id) {
        return convertToVO(
                assessmentJudgementRepository.findById(id)
                        .orElseThrow(() -> new DataNotFound("考核评判记录不存在，ID: " + id)));
    }

    @Override
    public AssessmentJudgementVO getLatestByAnswerId(Long answerId) {
        return convertToVO(
                assessmentJudgementRepository.findLatestByAnswerId(answerId)
                        .orElseThrow(() -> new DataNotFound("答案暂无评判记录，answerId: " + answerId)));
    }

    @Override
    public AssessmentJudgementVO getLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        return convertToVO(
                assessmentJudgementRepository.findLatestByQuestionIdAndUserId(questionId, userId)
                        .orElseThrow(() -> new DataNotFound("考生暂无该题评判记录")));
    }

    @Override
    public List<AssessmentJudgementVO> listByQuestionId(Long questionId) {
        return assessmentJudgementRepository.findAllByQuestionId(questionId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional
    public AssessmentJudgementVO finalizeJudgement(AssessmentJudgementVO judgement) {
        log.info(
                "finalize judgement for answer {}, question {}",
                judgement.getAnswerId(),
                judgement.getQuestionId());

        AssessmentJudgement entity = convertToEntity(judgement);
        LocalDateTime now = LocalDateTime.now();

        // 利用数据库唯一索引 + ON CONFLICT 实现原子性 upsert，消除并发竞态
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (entity.getJudgedAt() == null) {
            entity.setJudgedAt(now);
        }
        assessmentJudgementRepository.upsertAdminFinalized(entity);

        return convertToVO(
                assessmentJudgementRepository
                        .findLatestByAnswerIdAndSource(entity.getAnswerId(), JudgementSource.ADMIN_FINALIZED)
                        .orElseThrow(() -> new GlobalException("创建或更新最终评定记录失败")));
    }

    private AssessmentJudgement convertToEntity(AssessmentJudgementVO judgement) {
        AssessmentJudgement entity = AssessmentJudgement.create(
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
                judgement.getJudgedAt());
        entity.setId(judgement.getId());
        entity.setCreatedAt(judgement.getCreatedAt());
        entity.setUpdatedAt(judgement.getUpdatedAt());
        return entity;
    }

    private AssessmentJudgementVO convertToVO(AssessmentJudgement entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentJudgementVO.builder()
                .id(entity.getId())
                .answerId(entity.getAnswerId())
                .questionId(entity.getQuestionId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .userId(entity.getUserId())
                .score(entity.getScore())
                .maxScore(entity.getMaxScore())
                .status(entity.getStatus())
                .resultCode(entity.getResultCode())
                .source(entity.getSource())
                .reviewerId(entity.getReviewerId())
                .reviewerType(entity.getReviewerType())
                .judgedAt(entity.getJudgedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
