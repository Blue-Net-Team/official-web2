package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;

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

    /**
     * 查询题目维度评分汇总，算法题按最佳提交计入统计。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param questionType
     *            题型筛选
     * @param keyword
     *            题目关键词
     * @return 题目评分汇总列表
     */
    List<AssessmentQuestionScoreboardVO> findQuestionScoreboard(Long assessmentTimeId, QuestionType questionType,
            String keyword);

    /**
     * 查询题目下考生提交列表，附带每名考生当前应展示的评判记录。
     *
     * @param questionId
     *            题目ID
     * @param keyword
     *            考生关键词
     * @param status
     *            评判状态筛选
     * @return 提交列表
     */
    List<AssessmentQuestionSubmissionVO> findQuestionSubmissions(Long questionId, String keyword, String status);

    /**
     * 查询题目下指定考生的完整提交评判历史。
     *
     * @param questionId
     *            题目ID
     * @param userIds
     *            考生用户ID列表
     * @return 评判历史列表
     */
    List<AssessmentQuestionSubmissionHistoryVO> findQuestionSubmissionHistories(Long questionId, List<Long> userIds);

    /**
     * 查询考生维度评分矩阵的扁平行数据。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @return 考生评分矩阵行列表
     */
    List<AssessmentCandidateScoreRowVO> findCandidateScoreRows(Long assessmentTimeId, String keyword);
}
