package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
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
     * 保存新的考核评审结果 记录。
     *
     * @param judgement
     *            考核评审结果实体。
     */
    void save(AssessmentJudgement judgement);

    /**
     * 按主键查询考核评审结果 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    Optional<AssessmentJudgement> findById(Long id);

    /**
     * 查询指定作答的最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    Optional<AssessmentJudgement> findLatestByAnswerId(Long answerId);

    /**
     * 查询指定作答的指定来源最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @param source
     *            评判来源。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    Optional<AssessmentJudgement> findLatestByAnswerIdAndSource(Long answerId, JudgementSource source);

    /**
     * 批量查询哪些作答已有指定来源的评审结果。
     *
     * @param answerIds
     *            作答主键集合。
     * @param source
     *            评判来源。
     * @return 已有指定来源评审结果的作答主键集合。
     */
    List<Long> findAnswerIdsBySource(List<Long> answerIds, JudgementSource source);

    /**
     * 查询用户在指定题目上的最新评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 查询到的考核评审结果 实体；不存在时为空。
     */
    Optional<AssessmentJudgement> findLatestByQuestionIdAndUserId(Long questionId, Long userId);

    /**
     * 查询指定题目的全部评审记录。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 实体集合。
     */
    List<AssessmentJudgement> findAllByQuestionId(Long questionId);

    /**
     * 查询指定题目最新的客观题评审结果。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 实体集合。
     */
    List<AssessmentJudgement> findLatestObjectiveByQuestionId(Long questionId);

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionType
     *            题目类型过滤条件。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionScoreboardVO> findQuestionScoreboard(Long assessmentTimeId, QuestionType questionType,
            String keyword);

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param questionId
     *            考核题目主键。
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionSubmissionVO> findQuestionSubmissions(Long questionId, String keyword, String status);

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param questionId
     *            考核题目主键。
     * @param userIds
     *            候选用户主键集合。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionSubmissionHistoryVO> findQuestionSubmissionHistories(Long questionId, List<Long> userIds);

    /**
     * 查询符合条件的考核评审结果 记录。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentCandidateScoreRowVO> findCandidateScoreRows(Long assessmentTimeId, String keyword);

    /**
     * 按作答主键集合删除评审结果记录。
     *
     * @param answerIds
     *            作答主键集合。
     */
    void deleteByAnswerIds(List<Long> answerIds);

    /**
     * 批量保存评审结果记录。
     *
     * @param judgements
     *            评审结果实体列表。
     */
    void batchInsert(List<com.bluenet.web.domain.model.entity.AssessmentJudgement> judgements);

    /**
     * 插入或更新 ADMIN_FINALIZED 评审结果。
     * <p>
     * 利用数据库唯一索引实现原子性的 upsert，避免并发场景下重复插入。
     * </p>
     *
     * @param judgement
     *            评审结果实体。
     */
    void upsertAdminFinalized(AssessmentJudgement judgement);
}
