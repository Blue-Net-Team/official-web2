package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentCandidateScoreQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionScoreboardQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AssessmentQuestionSubmissionQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentJudgementMapper extends BaseMapper<AssessmentJudgementDO> {
    /**
     * 查询考核评审结果 数据行。
     *
     * @param answerId
     *            考核作答主键。
     * @return 匹配条件的考核评审结果 数据行；不存在时为 null。
     */
    AssessmentJudgementDO selectLatestByAnswerId(@Param("answerId") Long answerId);

    /**
     * 查询指定作答的指定来源最新评审结果。
     *
     * @param answerId
     *            考核作答主键。
     * @param source
     *            评判来源。
     * @return 匹配条件的考核评审结果 数据行；不存在时为 null。
     */
    AssessmentJudgementDO selectLatestByAnswerIdAndSource(@Param("answerId") Long answerId,
            @Param("source") JudgementSource source);

    /**
     * 批量查询哪些作答已有指定来源的评审结果。
     *
     * @param answerIds
     *            作答主键集合。
     * @param source
     *            评判来源。
     * @return 已有指定来源评审结果的作答主键列表。
     */
    List<Long> selectAnswerIdsBySource(@Param("answerIds") List<Long> answerIds,
            @Param("source") JudgementSource source);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param questionId
     *            考核题目主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 匹配条件的考核评审结果 数据行；不存在时为 null。
     */
    AssessmentJudgementDO selectLatestByQuestionIdAndUserId(@Param("questionId") Long questionId,
            @Param("userId") Long userId);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentJudgementDO> selectAllByQuestionId(@Param("questionId") Long questionId);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param questionId
     *            考核题目主键。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentJudgementDO> selectLatestObjectiveByQuestionId(@Param("questionId") Long questionId);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param questionType
     *            题目类型过滤条件。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionScoreboardQueryDO> selectQuestionScoreboard(
            @Param("assessmentTimeId") Long assessmentTimeId,
            @Param("questionType") QuestionType questionType,
            @Param("keyword") String keyword);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param questionId
     *            考核题目主键。
     * @param keyword
     *            搜索关键字。
     * @param status
     *            业务状态过滤条件。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionSubmissionQueryDO> selectQuestionSubmissions(
            @Param("questionId") Long questionId,
            @Param("keyword") String keyword,
            @Param("status") String status);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param questionId
     *            考核题目主键。
     * @param userIds
     *            候选用户主键集合。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentQuestionSubmissionQueryDO> selectQuestionSubmissionHistories(
            @Param("questionId") Long questionId,
            @Param("userIds") List<Long> userIds);

    /**
     * 查询考核评审结果 数据行。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @param keyword
     *            搜索关键字。
     * @return 满足条件的考核评审结果 结果集合。
     */
    List<AssessmentCandidateScoreQueryDO> selectCandidateScoreRows(
            @Param("assessmentTimeId") Long assessmentTimeId,
            @Param("keyword") String keyword);

    /**
     * 按作答主键集合删除评审结果数据行。
     *
     * @param answerIds
     *            作答主键集合。
     * @return 删除的记录数量。
     */
    int deleteByAnswerIds(@Param("answerIds") List<Long> answerIds);

    /**
     * 批量插入评审结果数据行。
     *
     * @param judgements
     *            评审结果数据对象列表。
     * @return 插入的记录数量。
     */
    int batchInsert(
            @Param("judgements") List<com.bluenet.web.infrastructure.repository.dataobject.AssessmentJudgementDO> judgements);
}
