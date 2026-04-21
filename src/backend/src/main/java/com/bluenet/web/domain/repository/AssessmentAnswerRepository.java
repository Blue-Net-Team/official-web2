package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AssessmentAnswerRepository {
    /**
     * 保存新的考核作答 记录。
     *
     * @param assessmentAnswer
     *            考核作答领域对象。
     */
    void save(AssessmentAnswer assessmentAnswer);
    /**
     * 按主键查询考核作答 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核作答 结果；不存在时为空。
     */
    Optional<AssessmentAnswerVO> findById(Long id);
    /**
     * 更新考核作答关联的提交文件。
     *
     * @param answerId
     *            考核作答主键。
     * @param fileId
     *            文件主键。
     * @return 数据库受影响行数。
     */
    int updateFileId(Long answerId, Long fileId);
    /**
     * 更新考核作答的提交时间。
     *
     * @param answerId
     *            考核作答主键。
     * @param submitTime
     *            作答提交时间。
     * @return 数据库受影响行数。
     */
    int updateSubmitTime(Long answerId, LocalDateTime submitTime);
    /**
     * 更新考核作答内容。
     *
     * @param answerId
     *            考核作答主键。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 数据库受影响行数。
     */
    int updateContent(Long answerId, String content);
    /**
     * 更新考核作答使用的编程语言。
     *
     * @param answerId
     *            考核作答主键。
     * @param language
     *            提交代码使用的编程语言。
     * @return 数据库受影响行数。
     */
    int updateLanguage(Long answerId, com.bluenet.web.domain.model.enumerate.ProgrammingLanguage language);
    /**
     * 统计用户在指定考核场次中已提交的作答数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的记录数量。
     */
    int countByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
    /**
     * 判断用户是否已经提交指定题目的作答。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);

    /**
     * 按用户和题目查询考核作答记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param questionId
     *            考核题目主键。
     * @return 查询到的考核作答 结果；不存在时为空。
     */
    Optional<AssessmentAnswerVO> findByUserIdAndQuestionId(Long userId, Long questionId);
}
