package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssessmentAnswerRepository {
    void save(AssessmentAnswer assessmentAnswer);
    Optional<AssessmentAnswer> findById(Long id);
    Optional<AssessmentAnswer> findByFileId(Long fileId);
    void update(AssessmentAnswer assessmentAnswer);
    int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);

    /**
     * 按用户和考核场次主键批量统计已完成答题数量。
     *
     * @param userId
     *            用户主键。
     * @param assessmentTimeIds
     *            考核场次主键列表。
     * @return 考核场次主键到已完成答题数量的映射；缺失主键视为 0。
     */
    Map<Long, Integer> countByUserIdAndAssessmentTimeIds(Long userId, List<Long> assessmentTimeIds);
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<AssessmentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
    List<AssessmentAnswer> findByTeamIdAndQuestionId(Long teamId, Long questionId);
    void deleteByTeamId(Long teamId);
    int countByTeamId(Long teamId);
    List<Long> findAnswerIdsByTeamId(Long teamId);
    void batchInsert(List<AssessmentAnswer> answers);
    int updateTeamMemberAnswers(Long teamId, Long questionId, Long fileId,
            String content, com.bluenet.web.domain.model.enumerate.ProgrammingLanguage language,
            java.time.LocalDateTime submitTime);
    List<Long> findExistingAnswerUserIds(List<Long> userIds, Long questionId);
    int countPersonalAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    int countTeamAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
}
