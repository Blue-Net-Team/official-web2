package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
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

    /**
     * 保存新的考核作答 记录。
     *
     * @param assessmentAnswer
     *            考核作答领域对象。
     */
    @Override
    public void save(AssessmentAnswer assessmentAnswer) {
        log.info("save assessment answer {}", assessmentAnswer);
        RepositoryObjectConverter.insert(assessmentAnswerMapper, assessmentAnswer, AssessmentAnswerDO.class);
    }

    /**
     * 按主键查询考核作答 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核作答 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentAnswerVO> findById(Long id) {
        AssessmentAnswer answer = RepositoryObjectConverter
                .toDomain(assessmentAnswerMapper.selectById(id), AssessmentAnswer.class);
        if (answer == null) {
            log.warn("assessment answer not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(answer));
    }

    /**
     * 按文件主键查询关联的考核作答记录。
     *
     * @param fileId
     *            文件主键。
     * @return 查询到的考核作答结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentAnswerVO> findByFileId(Long fileId) {
        AssessmentAnswer answer = RepositoryObjectConverter.toDomain(
                assessmentAnswerMapper.selectFirstByFileId(fileId),
                AssessmentAnswer.class);
        if (answer == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(answer));
    }

    /**
     * 更新考核作答关联的提交文件。
     *
     * @param answerId
     *            考核作答主键。
     * @param fileId
     *            文件主键。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateFileId(Long answerId, Long fileId) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setFileId(fileId);
        int influence = RepositoryObjectConverter.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);
        if (influence == 0) {
            log.warn("更新答题工作文件失败，保存到数据库时没有影响任何行，answerId {}, fileId {}", answerId, fileId);
            throw new GlobalException("更新答题工作文件失败");
        }
        return influence;
    }

    /**
     * 更新考核作答的提交时间。
     *
     * @param answerId
     *            考核作答主键。
     * @param submitTime
     *            作答提交时间。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateSubmitTime(Long answerId, LocalDateTime submitTime) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setSubmitTime(submitTime);
        int influence = RepositoryObjectConverter.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);
        if (influence == 0) {
            log.warn("更新答题提交时间失败，保存到数据库时没有影响任何行，answerId {}, submitTime {}", answerId, submitTime);
            throw new GlobalException("更新答题提交时间失败");
        }
        return influence;
    }

    /**
     * 更新考核作答内容。
     *
     * @param answerId
     *            考核作答主键。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateContent(Long answerId, String content) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setContent(content);
        int influence = RepositoryObjectConverter.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);
        if (influence == 0) {
            log.warn("更新答题内容失败，保存到数据库时没有影响任何行，answerId {}", answerId);
            throw new GlobalException("更新答题内容失败");
        }
        return influence;
    }

    /**
     * 更新考核作答使用的编程语言。
     *
     * @param answerId
     *            考核作答主键。
     * @param language
     *            提交代码使用的编程语言。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateLanguage(Long answerId, ProgrammingLanguage language) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(answerId);
        answer.setLanguage(language);
        int influence = RepositoryObjectConverter.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);
        if (influence == 0) {
            log.warn("更新答题语言失败，保存到数据库时没有影响任何行，answerId {}", answerId);
            throw new GlobalException("更新答题语言失败");
        }
        return influence;
    }

    /**
     * 统计用户在指定考核场次中已提交的作答数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    @Override
    public int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        return assessmentAnswerMapper.countByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
    }

    /**
     * 判断用户是否已经提交指定题目的作答。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByUserIdAndQuestionId(Long userId, Long questionId) {
        return assessmentAnswerMapper.countByUserIdAndQuestionId(userId, questionId) > 0;
    }

    /**
     * 按用户和题目查询考核作答记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 查询到的考核作答 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentAnswerVO> findByUserIdAndQuestionId(Long userId, Long questionId) {
        AssessmentAnswer answer = RepositoryObjectConverter.toDomain(
                assessmentAnswerMapper.selectByUserIdAndQuestionId(userId, questionId),
                AssessmentAnswer.class);
        if (answer == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(answer));
    }

    /**
     * 在考核作答 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param answer
     *            考核作答对象。
     * @return 转换后的目标模型对象。
     */
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
