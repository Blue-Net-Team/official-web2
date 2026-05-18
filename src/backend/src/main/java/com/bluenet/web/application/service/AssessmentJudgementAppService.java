package com.bluenet.web.application.service;

import com.bluenet.web.application.AssessmentDecisionResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionWorkspaceVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;

import java.util.List;

/**
 * 考核评判应用服务接口。
 * <p>
 * 定义了考核评判聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentJudgementAppService {

    /**
     * 查询答案最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判结果
     */
    AssessmentJudgementResult getLatestByAnswerId(Long answerId);

    /**
     * 查询题目下的评判结果列表。
     *
     * @param questionId
     *            题目ID
     * @return 评判结果列表
     */
    List<AssessmentJudgementResult> listByQuestionId(Long questionId);

    /**
     * 设置考生最终通过决策。
     *
     * @param command
     *            最终通过决策命令
     * @return 决策结果
     */
    AssessmentDecisionResult decideAssessment(AssessmentJudgementCommands.DecideAssessmentCommand command);

    /**
     * 查询指定考核时间下的题目评分汇总。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param questionType
     *            题目类型
     * @param keyword
     *            关键词
     * @return 题目评分汇总列表
     */
    List<AssessmentQuestionScoreboardVO> listQuestionScoreboard(
            Long assessmentTimeId,
            QuestionType questionType,
            String keyword);

    /**
     * 查询指定题目的所有提交与最新评判。
     *
     * @param questionId
     *            题目ID
     * @param keyword
     *            关键词
     * @param status
     *            状态
     * @return 题目提交与评判列表
     */
    List<AssessmentQuestionSubmissionVO> listQuestionSubmissions(Long questionId, String keyword, String status);

    /**
     * 查询指定考核时间下的考生评分矩阵。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            关键词
     * @return 考生评分矩阵列表
     */
    List<AssessmentCandidateScoreboardVO> listCandidateScoreboard(Long assessmentTimeId, String keyword);

    /**
     * 查询指定考核时间下的录用决策工作台数据。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            关键词
     * @param decisionStatus
     *            决策状态
     * @return 录用决策工作台数据
     */
    AssessmentDecisionWorkspaceVO getDecisionWorkspace(Long assessmentTimeId, String keyword, String decisionStatus);

    /**
     * 发布指定考核轮次的决策结果邮件通知。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 成功发送的邮件数量
     */
    int publishDecisions(Long assessmentTimeId);

    /**
     * 方向管理员确认文件上传题的最终评分。
     *
     * @param command
     *            最终评分命令
     * @return 评判结果
     */
    AssessmentJudgementResult finalizeScore(AssessmentJudgementCommands.FinalizeScoreCommand command);
}
