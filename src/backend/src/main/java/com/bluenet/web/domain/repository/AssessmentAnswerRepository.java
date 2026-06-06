package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;

import java.util.List;
import java.util.Optional;

public interface AssessmentAnswerRepository {
    void save(AssessmentAnswer assessmentAnswer);
    Optional<AssessmentAnswer> findById(Long id);
    Optional<AssessmentAnswer> findByFileId(Long fileId);
    void update(AssessmentAnswer assessmentAnswer);
    int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<AssessmentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
    List<AssessmentAnswer> findByTeamIdAndQuestionId(Long teamId, Long questionId);
    void deleteByTeamId(Long teamId);
    int countByTeamId(Long teamId);
    List<Long> findAnswerIdsByTeamId(Long teamId);
    void batchInsert(List<AssessmentAnswer> answers);
    int updateTeamMemberAnswers(Long teamId, Long leaderId, Long questionId, Long fileId,
            String content, com.bluenet.web.domain.model.enumerate.ProgrammingLanguage language,
            java.time.LocalDateTime submitTime);
    List<Long> findExistingAnswerUserIds(List<Long> userIds, Long questionId);
    int countPersonalAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    int countTeamAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
}
