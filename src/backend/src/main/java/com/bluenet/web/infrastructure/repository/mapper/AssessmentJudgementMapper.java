package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentJudgementMapper extends BaseMapper<AssessmentJudgement> {
    /**
     * 查询答案的最新评判记录。
     */
    AssessmentJudgement selectLatestByAnswerId(@Param("answerId") Long answerId);

    /**
     * 查询考生在某道题上的最新评判记录。
     */
    AssessmentJudgement selectLatestByQuestionIdAndUserId(@Param("questionId") Long questionId,
            @Param("userId") Long userId);

    /**
     * 查询题目下所有评判记录。
     */
    List<AssessmentJudgement> selectAllByQuestionId(@Param("questionId") Long questionId);

    /**
     * 查询客观题每个考生的最新自动评判记录。
     */
    List<AssessmentJudgement> selectLatestObjectiveByQuestionId(@Param("questionId") Long questionId);

    /**
     * 查询题目维度评分汇总，算法题按最佳提交计入统计。
     */
    List<AssessmentQuestionScoreboardVO> selectQuestionScoreboard(
            @Param("assessmentTimeId") Long assessmentTimeId,
            @Param("questionType") QuestionType questionType,
            @Param("keyword") String keyword);

    /**
     * 查询题目下考生提交列表，附带每名考生当前应展示的评判记录。
     */
    List<AssessmentQuestionSubmissionVO> selectQuestionSubmissions(
            @Param("questionId") Long questionId,
            @Param("keyword") String keyword,
            @Param("status") String status);

    /**
     * 查询题目下指定考生的完整提交评判历史。
     */
    List<AssessmentQuestionSubmissionVO> selectQuestionSubmissionHistories(
            @Param("questionId") Long questionId,
            @Param("userIds") List<Long> userIds);

    /**
     * 查询考生维度评分矩阵的扁平行数据。
     */
    List<AssessmentCandidateScoreRowVO> selectCandidateScoreRows(
            @Param("assessmentTimeId") Long assessmentTimeId,
            @Param("keyword") String keyword);
}
