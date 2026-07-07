package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentScope;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentDecisionDomainServiceImpl implements AssessmentDecisionDomainService {
    private final AssessmentDecisionRepository assessmentDecisionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;

    @Override
    public boolean isEliminatedFromPriorEpoch(Long userId, AssessmentTime targetTime) {
        List<AssessmentDecision> eliminatedDecisions = assessmentDecisionRepository
                .findEliminatedDecisionsByUserId(userId);
        if (eliminatedDecisions.isEmpty()) {
            return false;
        }
        List<Long> decisionTimeIds = eliminatedDecisions.stream()
                .map(AssessmentDecision::getAssessmentTimeId)
                .distinct()
                .toList();
        Map<Long, AssessmentTime> decisionTimeMap = assessmentTimeRepository.findAllById(decisionTimeIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(AssessmentTime::getId, time -> time));
        return isEliminatedFromPriorEpoch(targetTime, eliminatedDecisions, decisionTimeMap);
    }

    @Override
    public boolean isEliminatedFromPriorEpoch(AssessmentTime targetTime, List<AssessmentDecision> eliminatedDecisions,
            Map<Long, AssessmentTime> decisionTimeMap) {
        if (eliminatedDecisions == null || eliminatedDecisions.isEmpty()) {
            return false;
        }
        for (AssessmentDecision decision : eliminatedDecisions) {
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
}
