package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;

import java.util.List;
import java.util.Optional;

/**
 * 考核题目评判记录仓储接口。
 */
public interface AssessmentJudgementRepository {

    /**
     * 保存新的题目评判记录。
     *
     * @param judgement
     *            评判记录实体
     */
    void save(AssessmentJudgement judgement);

    /**
     * 按主键查询评判记录。
     *
     * @param id
     *            评判记录ID
     * @return 评判记录
     */
    Optional<AssessmentJudgementVO> findById(Long id);

    /**
     * 更新已有评判记录。
     *
     * @param judgement
     *            评判记录VO
     */
    void update(AssessmentJudgementVO judgement);

    /**
     * 查询一个答案最新的评判记录。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判记录
     */
    Optional<AssessmentJudgementVO> findLatestByAnswerId(Long answerId);

    /**
     * 查询一个考生在一道题上的最新评判记录。
     *
     * @param questionId
     *            题目ID
     * @param userId
     *            考生用户ID
     * @return 最新评判记录
     */
    Optional<AssessmentJudgementVO> findLatestByQuestionIdAndUserId(Long questionId, Long userId);

    /**
     * 查询一道题的所有评判记录。
     *
     * @param questionId
     *            题目ID
     * @return 评判记录列表
     */
    List<AssessmentJudgementVO> findAllByQuestionId(Long questionId);

    /**
     * 查询一道客观题每个考生最新的正式自动评判记录。
     *
     * @param questionId
     *            题目ID
     * @return 每名考生最新自动评判记录
     */
    List<AssessmentJudgementVO> findLatestObjectiveByQuestionId(Long questionId);
}
