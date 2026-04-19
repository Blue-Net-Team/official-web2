package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
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

        return assessmentJudgementRepository.findById(entity.getId())
                .orElseThrow(() -> new GlobalException("创建考核评判记录失败"));
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
        assessmentJudgementRepository.update(judgement);
        return assessmentJudgementRepository.findById(judgement.getId())
                .orElseThrow(() -> new GlobalException("更新考核评判记录失败"));
    }

    @Override
    public AssessmentJudgementVO getJudgementById(Long id) {
        return assessmentJudgementRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考核评判记录不存在，ID: " + id));
    }

    @Override
    public AssessmentJudgementVO getLatestByAnswerId(Long answerId) {
        return assessmentJudgementRepository.findLatestByAnswerId(answerId)
                .orElseThrow(() -> new DataNotFound("答案暂无评判记录，answerId: " + answerId));
    }

    @Override
    public AssessmentJudgementVO getLatestByQuestionIdAndUserId(Long questionId, Long userId) {
        return assessmentJudgementRepository.findLatestByQuestionIdAndUserId(questionId, userId)
                .orElseThrow(() -> new DataNotFound("考生暂无该题评判记录"));
    }

    @Override
    public List<AssessmentJudgementVO> listByQuestionId(Long questionId) {
        return assessmentJudgementRepository.findAllByQuestionId(questionId);
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
}
