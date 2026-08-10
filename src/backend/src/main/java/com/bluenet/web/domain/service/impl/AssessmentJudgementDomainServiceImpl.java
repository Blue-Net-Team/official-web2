package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.service.AssessmentJudgementDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 考核题目评判领域服务实现。
 * <p>
 * 仅保留最终评分确认的原子性 upsert 逻辑。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentJudgementDomainServiceImpl implements AssessmentJudgementDomainService {
    private final AssessmentJudgementRepository assessmentJudgementRepository;

    @Override
    @Transactional
    public AssessmentJudgement finalizeJudgement(AssessmentJudgement judgement) {
        log.info(
                "finalize judgement for answer {}, question {}",
                judgement.getAnswerId(),
                judgement.getQuestionId());

        LocalDateTime now = LocalDateTime.now();
        judgement.setCreatedAt(now);
        judgement.setUpdatedAt(now);
        if (judgement.getJudgedAt() == null) {
            judgement.setJudgedAt(now);
        }
        assessmentJudgementRepository.upsertAdminFinalized(judgement);

        return assessmentJudgementRepository
                .findLatestByAnswerIdAndSource(judgement.getAnswerId(), JudgementSource.ADMIN_FINALIZED)
                .orElseThrow(() -> new GlobalException("创建或更新最终评定记录失败"));
    }
}
