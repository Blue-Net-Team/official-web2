package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.infrastructure.repository.converter.AssessmentAnswerRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerRepositoryImpl implements AssessmentAnswerRepository {
    private final AssessmentAnswerMapper assessmentAnswerMapper;
    private final AssessmentAnswerRepositoryConverter converter;

    @Override
    public void save(AssessmentAnswer assessmentAnswer) {
        log.info("save assessment answer {}", assessmentAnswer);
        AssessmentAnswerDO dataObject = converter.toDataObject(assessmentAnswer);
        assessmentAnswerMapper.insert(dataObject);
        assessmentAnswer.setId(dataObject.getId());
    }

    @Override
    public Optional<AssessmentAnswer> findById(Long id) {
        AssessmentAnswerDO dataObject = assessmentAnswerMapper.selectById(id);
        if (dataObject == null) {
            log.warn("assessment answer not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(converter.toEntity(dataObject));
    }

    @Override
    public Optional<AssessmentAnswer> findByFileId(Long fileId) {
        AssessmentAnswerDO dataObject = assessmentAnswerMapper.selectFirstByFileId(fileId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void update(AssessmentAnswer assessmentAnswer) {
        AssessmentAnswerDO dataObject = converter.toDataObject(assessmentAnswer);
        int influence = assessmentAnswerMapper.updateById(dataObject);
        if (influence == 0) {
            log.warn("更新答题失败，保存到数据库时没有影响任何行，answerId {}", assessmentAnswer.getId());
            throw new GlobalException("更新答题失败");
        }
    }

    @Override
    public int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        return assessmentAnswerMapper.countByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
    }

    @Override
    public boolean existsByUserIdAndQuestionId(Long userId, Long questionId) {
        return assessmentAnswerMapper.countByUserIdAndQuestionId(userId, questionId) > 0;
    }

    @Override
    public Optional<AssessmentAnswer> findByUserIdAndQuestionId(Long userId, Long questionId) {
        AssessmentAnswerDO dataObject = assessmentAnswerMapper.selectByUserIdAndQuestionId(userId, questionId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public List<AssessmentAnswer> findByTeamIdAndQuestionId(Long teamId, Long questionId) {
        List<AssessmentAnswerDO> dataObjects = assessmentAnswerMapper.selectByTeamIdAndQuestionId(teamId, questionId);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public void deleteByTeamId(Long teamId) {
        assessmentAnswerMapper.deleteByTeamId(teamId);
    }

    @Override
    public int countByTeamId(Long teamId) {
        return assessmentAnswerMapper.countByTeamId(teamId);
    }

    @Override
    public List<Long> findAnswerIdsByTeamId(Long teamId) {
        return assessmentAnswerMapper.selectAnswerIdsByTeamId(teamId);
    }

    @Override
    public void batchInsert(List<AssessmentAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        List<AssessmentAnswerDO> dataObjects = converter.toDataObjectList(answers);
        assessmentAnswerMapper.batchInsert(dataObjects);
        // Note: batch insert does not populate IDs automatically for MySQL/PostgreSQL
        // The caller should not rely on IDs being set after batch insert
    }

    @Override
    public int updateTeamMemberAnswers(Long teamId, Long leaderId, Long questionId, Long fileId,
            String content, com.bluenet.web.domain.model.enumerate.ProgrammingLanguage language,
            java.time.LocalDateTime submitTime) {
        return assessmentAnswerMapper.updateTeamMemberAnswers(
                teamId,
                leaderId,
                questionId,
                fileId,
                content,
                language,
                submitTime);
    }

    @Override
    public List<Long> findExistingAnswerUserIds(List<Long> userIds, Long questionId) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return assessmentAnswerMapper.selectExistingAnswerUserIds(userIds, questionId);
    }

    @Override
    public int countPersonalAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        return assessmentAnswerMapper.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
    }

    @Override
    public int countTeamAnswersByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        return assessmentAnswerMapper.countTeamAnswersByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
    }
}
