package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AssessmentAnswerRepository {
    void save(AssessmentAnswer assessmentAnswer);
    Optional<AssessmentAnswerVO> findById(Long id);
    int updateFileId(Long answerId, Long fileId);
    int updateSubmitTime(Long answerId, LocalDateTime submitTime);
    int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
}
