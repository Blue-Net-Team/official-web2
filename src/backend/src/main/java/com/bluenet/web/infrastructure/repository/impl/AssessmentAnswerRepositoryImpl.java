package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentAnswerRepositoryImpl implements AssessmentAnswerRepository {
    private final AssessmentAnswerMapper assessmentAnswerMapper;

    @Override
    public void save(AssessmentAnswer assessmentAnswer) {
        log.info("save assessment answer {}", assessmentAnswer);
        assessmentAnswerMapper.insert(assessmentAnswer);
    }

    @Override
    public Optional<AssessmentAnswerVO> findById(Long id) {
        AssessmentAnswer answer = assessmentAnswerMapper.selectById(id);
        if (answer == null) {
            log.warn("assessment answer not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(answer));
    }

    @Override
    public int updateFileId(Long answerId, Long fileId) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setFileId(fileId);
        int influence = assessmentAnswerMapper.updateById(answer);
        if (influence == 0) {
            log.warn("更新答题工作文件失败，保存到数据库时没有影响任何行，answerId {}, fileId {}", answerId, fileId);
            throw new GlobalException("更新答题工作文件失败");
        }
        return influence;
    }

    @Override
    public int updateSubmitTime(Long answerId, LocalDateTime submitTime) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setSubmitTime(submitTime);
        int influence = assessmentAnswerMapper.updateById(answer);
        if (influence == 0) {
            log.warn("更新答题提交时间失败，保存到数据库时没有影响任何行，answerId {}, submitTime {}", answerId, submitTime);
            throw new GlobalException("更新答题提交时间失败");
        }
        return influence;
    }

    @Override
    public int updateContent(Long answerId, String content) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setContent(content);
        int influence = assessmentAnswerMapper.updateById(answer);
        if (influence == 0) {
            log.warn("更新答题内容失败，保存到数据库时没有影响任何行，answerId {}", answerId);
            throw new GlobalException("更新答题内容失败");
        }
        return influence;
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
    public Optional<AssessmentAnswerVO> findByUserIdAndQuestionId(Long userId, Long questionId) {
        AssessmentAnswer answer = assessmentAnswerMapper.selectByUserIdAndQuestionId(userId, questionId);
        if (answer == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(answer));
    }

    private AssessmentAnswerVO convertToVO(AssessmentAnswer answer) {
        return AssessmentAnswerVO.builder()
                .id(answer.getId())
                .userId(answer.getUserId())
                .questionId(answer.getQuestionId())
                .content(answer.getContent())
                .language(answer.getLanguage())
                .fileId(answer.getFileId())
                .submitTime(answer.getSubmitTime())
                .build();
    }
}
