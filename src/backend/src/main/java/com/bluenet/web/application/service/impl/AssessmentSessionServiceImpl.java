package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;
import com.bluenet.web.application.service.AssessmentSessionService;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.service.AssessmentSessionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionServiceImpl implements AssessmentSessionService {

    private final AssessmentSessionDomainService assessmentSessionDomainService;

    @Override
    public AssessmentSessionDTO getOrCreateSession(Long userId, Long assessmentTimeId) {
        AssessmentSessionVO session = assessmentSessionDomainService.getOrCreateSession(userId, assessmentTimeId);
        if (session == null) {
            return null;
        }
        return AssessmentSessionDTO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .assessmentTimeId(session.getAssessmentTimeId())
                .startTime(session.getStartTime())
                .deadline(session.getDeadline())
                .build();
    }
}
