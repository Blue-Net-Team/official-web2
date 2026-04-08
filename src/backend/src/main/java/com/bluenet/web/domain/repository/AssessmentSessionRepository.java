package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;

import java.util.Optional;

public interface AssessmentSessionRepository {
    void save(AssessmentSession session);

    Optional<AssessmentSessionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
}
