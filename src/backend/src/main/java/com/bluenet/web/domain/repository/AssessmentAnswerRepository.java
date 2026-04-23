package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;

import java.util.Optional;

public interface AssessmentAnswerRepository {
    void save(AssessmentAnswer assessmentAnswer);
    Optional<AssessmentAnswer> findById(Long id);
    Optional<AssessmentAnswer> findByFileId(Long fileId);
    void update(AssessmentAnswer assessmentAnswer);
    int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<AssessmentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
}
