package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionRepositoryImpl implements AssessmentSessionRepository {
    private final AssessmentSessionMapper assessmentSessionMapper;

    @Override
    public void save(AssessmentSession session) {
        log.info(
                "save assessment session for userId: {}, assessmentTimeId: {}",
                session.getUserId(),
                session.getAssessmentTimeId());
        assessmentSessionMapper.insert(session);
    }

    @Override
    public Optional<AssessmentSessionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        AssessmentSession session = assessmentSessionMapper.selectByUserIdAndAssessmentTimeId(
                userId,
                assessmentTimeId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(session));
    }

    private AssessmentSessionVO convertToVO(AssessmentSession session) {
        return AssessmentSessionVO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .assessmentTimeId(session.getAssessmentTimeId())
                .startTime(session.getStartTime())
                .deadline(session.getDeadline())
                .build();
    }
}
