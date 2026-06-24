package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentScope;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentDecisionDomainServiceImpl implements AssessmentDecisionDomainService {
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;

    @Override
    @Transactional
    public AssessmentDecisionVO saveDecision(AssessmentDecisionVO decision) {
        log.info(
                "save assessment decision for user {}, time {}",
                decision.getUserId(),
                decision.getAssessmentTimeId());
        LocalDateTime now = LocalDateTime.now();
        AssessmentDecisionVO existing = assessmentDecisionRepository
                .findByUserIdAndAssessmentTimeId(decision.getUserId(), decision.getAssessmentTimeId())
                .orElse(null);

        if (existing == null) {
            AssessmentDecision entity = convertToEntity(decision);
            entity.setDecidedAt(now);
            entity.setUpdatedAt(now);
            assessmentDecisionRepository.save(entity);
            return assessmentDecisionRepository.findById(entity.getId())
                    .orElseThrow(() -> new GlobalException("保存考核通过决策失败"));
        }

        // 最终通过决策按考生和考核时间唯一，重复设置时覆盖最新决策内容。
        AssessmentDecisionVO updated = AssessmentDecisionVO.builder()
                .id(existing.getId())
                .userId(existing.getUserId())
                .assessmentTimeId(existing.getAssessmentTimeId())
                .passed(decision.getPassed())
                .decidedBy(decision.getDecidedBy())
                .decisionComment(decision.getDecisionComment())
                .decidedAt(now)
                .updatedAt(now)
                .build();
        assessmentDecisionRepository.update(updated);
        return assessmentDecisionRepository.findById(existing.getId())
                .orElseThrow(() -> new GlobalException("更新考核通过决策失败"));
    }

    @Override
    public AssessmentDecisionVO getDecisionById(Long id) {
        return assessmentDecisionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("考核通过决策不存在，ID: " + id));
    }

    @Override
    public AssessmentDecisionVO getDecision(Long userId, Long assessmentTimeId) {
        return assessmentDecisionRepository.findByUserIdAndAssessmentTimeId(userId, assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考生暂无该次考核通过决策"));
    }

    @Override
    public boolean isEliminatedFromPriorEpoch(Long userId, AssessmentTime targetTime) {
        List<AssessmentDecisionVO> eliminatedDecisions = assessmentDecisionRepository
                .findEliminatedDecisionsByUserId(userId);
        if (eliminatedDecisions.isEmpty()) {
            return false;
        }
        List<Long> decisionTimeIds = eliminatedDecisions.stream()
                .map(AssessmentDecisionVO::getAssessmentTimeId)
                .distinct()
                .toList();
        Map<Long, AssessmentTime> decisionTimeMap = assessmentTimeRepository.findAllById(decisionTimeIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(AssessmentTime::getId, time -> time));
        return isEliminatedFromPriorEpoch(targetTime, eliminatedDecisions, decisionTimeMap);
    }

    @Override
    public boolean isEliminatedFromPriorEpoch(AssessmentTime targetTime, List<AssessmentDecisionVO> eliminatedDecisions,
            Map<Long, AssessmentTime> decisionTimeMap) {
        if (eliminatedDecisions == null || eliminatedDecisions.isEmpty()) {
            return false;
        }
        for (AssessmentDecisionVO decision : eliminatedDecisions) {
            AssessmentTime decisionTime = decisionTimeMap.get(decision.getAssessmentTimeId());
            if (decisionTime == null) {
                continue;
            }
            if (isSameDirectionAndGrade(decisionTime, targetTime)
                    && isPriorEpoch(decisionTime, targetTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameDirectionAndGrade(AssessmentTime eliminatedTime, AssessmentTime targetTime) {
        return eliminatedTime.getScope().matches(targetTime.getScope())
                && eliminatedTime.matchesGrade(targetTime);
    }

    private boolean isPriorEpoch(AssessmentTime priorTime, AssessmentTime currentTime) {
        AssessmentScope priorScope = priorTime.getScope();
        if (!priorScope.isValidDirectionalEpoch()) {
            return false;
        }
        AssessmentScope currentScope = currentTime.getScope();
        if (currentScope.isFinalRound()) {
            return true;
        }
        Integer currentEpoch = currentTime.getEpoch();
        return currentEpoch != null && priorTime.getEpoch() < currentEpoch;
    }

    private AssessmentDecision convertToEntity(AssessmentDecisionVO decision) {
        return AssessmentDecision.reconstruct(
                decision.getId(),
                decision.getUserId(),
                decision.getAssessmentTimeId(),
                decision.getPassed(),
                decision.getDecidedBy(),
                decision.getDecisionComment(),
                decision.getDecidedAt(),
                decision.getUpdatedAt());
    }
}
