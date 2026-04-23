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
}
