package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateQuestionScoreDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionCandidateDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionStatisticsDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionHistoryDTO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreRowVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.AssessmentJudgementVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 考核评判转换器。
 * <p>
 * 负责评判、提交、评分矩阵和决策工作台相关 VO 到接口 DTO 的转换与展示聚合。
 * </p>
 */
@Component
public class AssessmentJudgementConverter {
    /**
     * 将题目评分汇总 VO 转换为接口 DTO。
     *
     * @param vo
     *            题目评分汇总 VO
     * @return 对应的接口 DTO
     */
    public AssessmentQuestionScoreboardDTO convertScoreboardToDTO(AssessmentQuestionScoreboardVO vo) {
        return AssessmentQuestionScoreboardDTO.builder()
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionType(vo.getQuestionType())
                .title(vo.getTitle())
                .maxScore(vo.getMaxScore())
                .submittedCount(vo.getSubmittedCount())
                .judgedCount(vo.getJudgedCount())
                .pendingCount(vo.getPendingCount())
                .averageScore(vo.getAverageScore())
                .build();
    }

    /**
     * 将题目提交 VO 转换为接口 DTO，内嵌最新评判信息。
     *
     * @param vo
     *            题目提交 VO
     * @return 对应的接口 DTO
     */
    public AssessmentQuestionSubmissionDTO convertSubmissionToDTO(AssessmentQuestionSubmissionVO vo) {
        return AssessmentQuestionSubmissionDTO.builder()
                .answerId(vo.getAnswerId())
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .questionNo(vo.getQuestionNo())
                .questionTitle(vo.getQuestionTitle())
                .questionType(vo.getQuestionType())
                .maxScore(vo.getMaxScore())
                .candidateUserId(vo.getCandidateUserId())
                .studentId(vo.getStudentId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .fileId(vo.getFileId())
                .content(vo.getContent())
                .language(vo.getLanguage())
                .submitTime(vo.getSubmitTime())
                .latestJudgement(convertJudgementFromSubmission(vo))
                .build();
    }

    /**
     * 将评判历史 VO 转换为接口 DTO。
     *
     * @param vo
     *            评判历史 VO
     * @return 对应的接口 DTO
     */
    public AssessmentQuestionSubmissionHistoryDTO convertSubmissionHistoryToDTO(
            AssessmentQuestionSubmissionHistoryVO vo) {
        return AssessmentQuestionSubmissionHistoryDTO.builder()
                .judgement(convertToDTO(vo.getJudgement()))
                .selectedBest(Boolean.TRUE.equals(vo.getSelectedBest()))
                .build();
    }

    /**
     * 将考生维度扁平行数据按考生聚合，计算总分、已评和待评数量。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param rows
     *            考生评分扁平行 VO 列表
     * @return 按考生聚合的评分汇总 DTO 列表
     */
    public List<AssessmentCandidateScoreboardDTO> buildCandidateScoreboards(
            Long assessmentTimeId,
            List<AssessmentCandidateScoreRowVO> rows) {
        Map<Long, List<AssessmentCandidateScoreRowVO>> grouped = rows.stream()
                .collect(
                        Collectors.groupingBy(
                                AssessmentCandidateScoreRowVO::getCandidateUserId,
                                LinkedHashMap::new,
                                Collectors.toList()));
        List<AssessmentCandidateScoreboardDTO> result = new ArrayList<>();
        for (List<AssessmentCandidateScoreRowVO> candidateRows : grouped.values()) {
            AssessmentCandidateScoreRowVO first = candidateRows.get(0);
            List<AssessmentCandidateQuestionScoreDTO> questionScores = candidateRows.stream()
                    .map(row -> convertQuestionScore(assessmentTimeId, row))
                    .toList();
            BigDecimal totalScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreDTO::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal maxScore = questionScores.stream()
                    .map(AssessmentCandidateQuestionScoreDTO::getMaxScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long judgedCount = questionScores.stream()
                    .filter(score -> Boolean.TRUE.equals(score.getJudged()))
                    .count();
            long pendingCount = questionScores.stream()
                    .filter(
                            score -> Boolean.TRUE.equals(score.getSubmitted())
                                    && !Boolean.TRUE.equals(score.getJudged()))
                    .count();
            result.add(
                    AssessmentCandidateScoreboardDTO.builder()
                            .candidateUserId(first.getCandidateUserId())
                            .studentId(first.getStudentId())
                            .username(first.getUsername())
                            .nickname(first.getNickname())
                            .totalScore(totalScore)
                            .maxScore(maxScore)
                            .judgedQuestionCount(judgedCount)
                            .pendingJudgementCount(pendingCount)
                            .questionScores(questionScores)
                            .build());
        }
        return result;
    }

    /**
     * 将评分汇总和决策记录组合为候选人 DTO。
     *
     * @param scoreboard
     *            考生评分汇总 DTO
     * @param decision
     *            已有决策 VO，无决策时为 null
     * @return 候选人 DTO
     */
    public AssessmentDecisionCandidateDTO convertDecisionCandidate(
            AssessmentCandidateScoreboardDTO scoreboard,
            AssessmentDecisionVO decision) {
        return AssessmentDecisionCandidateDTO.builder()
                .candidateUserId(scoreboard.getCandidateUserId())
                .studentId(scoreboard.getStudentId())
                .username(scoreboard.getUsername())
                .nickname(scoreboard.getNickname())
                .totalScore(scoreboard.getTotalScore())
                .maxScore(scoreboard.getMaxScore())
                .judgedQuestionCount(scoreboard.getJudgedQuestionCount())
                .pendingJudgementCount(scoreboard.getPendingJudgementCount())
                .decisionId(decision == null ? null : decision.getId())
                .passed(decision == null ? null : decision.getPassed())
                .decisionComment(decision == null ? null : decision.getDecisionComment())
                .decidedBy(decision == null ? null : decision.getDecidedBy())
                .decidedAt(decision == null ? null : decision.getDecidedAt())
                .questionScores(scoreboard.getQuestionScores())
                .build();
    }

    /**
     * 根据评分矩阵和决策记录计算候选人、待决策、通过和淘汰的统计数据。
     *
     * @param scoreboards
     *            考生评分汇总列表
     * @param decisions
     *            按用户ID索引的决策记录映射
     * @return 决策统计 DTO
     */
    public AssessmentDecisionStatisticsDTO calculateDecisionStatistics(
            List<AssessmentCandidateScoreboardDTO> scoreboards,
            Map<Long, AssessmentDecisionVO> decisions) {
        long candidates = scoreboards.size();
        long passed = scoreboards.stream()
                .map(scoreboard -> decisions.get(scoreboard.getCandidateUserId()))
                .filter(Objects::nonNull)
                .filter(decision -> Boolean.TRUE.equals(decision.getPassed()))
                .count();
        long eliminated = scoreboards.stream()
                .map(scoreboard -> decisions.get(scoreboard.getCandidateUserId()))
                .filter(Objects::nonNull)
                .filter(decision -> Boolean.FALSE.equals(decision.getPassed()))
                .count();
        return AssessmentDecisionStatisticsDTO.builder()
                .candidates(candidates)
                .pending(candidates - passed - eliminated)
                .passed(passed)
                .eliminated(eliminated)
                .build();
    }

    /**
     * 将评判 VO 转换为接口 DTO。
     *
     * @param judgement
     *            评判 VO，为 null 时返回 null
     * @return 对应的接口 DTO
     */
    public AssessmentJudgementDTO convertToDTO(AssessmentJudgementVO judgement) {
        if (judgement == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(judgement.getId())
                .answerId(judgement.getAnswerId())
                .questionId(judgement.getQuestionId())
                .assessmentTimeId(judgement.getAssessmentTimeId())
                .userId(judgement.getUserId())
                .score(judgement.getScore())
                .maxScore(judgement.getMaxScore())
                .status(judgement.getStatus())
                .resultCode(judgement.getResultCode())
                .source(judgement.getSource())
                .reviewerId(judgement.getReviewerId())
                .reviewerType(judgement.getReviewerType())
                .comment(judgement.getComment())
                .judgedAt(judgement.getJudgedAt())
                .build();
    }

    /**
     * 将决策 VO 转换为接口 DTO。
     *
     * @param decision
     *            决策 VO
     * @return 对应的接口 DTO
     */
    public AssessmentDecisionDTO convertToDTO(AssessmentDecisionVO decision) {
        return AssessmentDecisionDTO.builder()
                .id(decision.getId())
                .userId(decision.getUserId())
                .assessmentTimeId(decision.getAssessmentTimeId())
                .passed(decision.getPassed())
                .decidedBy(decision.getDecidedBy())
                .decisionComment(decision.getDecisionComment())
                .decidedAt(decision.getDecidedAt())
                .build();
    }

    /**
     * 将考生单题评分行 VO 转换为接口 DTO。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param row
     *            考生单题评分行 VO
     * @return 单题评分状态 DTO
     */
    private AssessmentCandidateQuestionScoreDTO convertQuestionScore(
            Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        boolean submitted = row.getAnswerId() != null;
        boolean judged = row.getJudgementId() != null;
        return AssessmentCandidateQuestionScoreDTO.builder()
                .questionId(row.getQuestionId())
                .questionNo(row.getQuestionNo())
                .questionTitle(row.getQuestionTitle())
                .questionType(row.getQuestionType())
                .maxScore(row.getMaxScore())
                .answerId(row.getAnswerId())
                .submitted(submitted)
                .submitTime(row.getSubmitTime())
                .score(row.getJudgementScore())
                .judged(judged)
                .latestJudgement(convertJudgementFromScoreRow(assessmentTimeId, row))
                .build();
    }

    /**
     * 从提交 VO 中提取评判信息转换为 DTO。
     */
    private AssessmentJudgementDTO convertJudgementFromSubmission(AssessmentQuestionSubmissionVO vo) {
        if (vo.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(vo.getJudgementId())
                .answerId(vo.getAnswerId())
                .questionId(vo.getQuestionId())
                .assessmentTimeId(vo.getAssessmentTimeId())
                .userId(vo.getCandidateUserId())
                .score(vo.getJudgementScore())
                .maxScore(vo.getJudgementMaxScore())
                .status(vo.getJudgementStatus())
                .resultCode(vo.getResultCode())
                .source(vo.getSource())
                .reviewerId(vo.getReviewerId())
                .reviewerType(vo.getReviewerType())
                .comment(vo.getJudgementComment())
                .judgedAt(vo.getJudgedAt())
                .build();
    }

    /**
     * 从考生评分行 VO 中提取评判信息转换为 DTO。
     */
    private AssessmentJudgementDTO convertJudgementFromScoreRow(Long assessmentTimeId,
            AssessmentCandidateScoreRowVO row) {
        if (row.getJudgementId() == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(row.getJudgementId())
                .answerId(row.getAnswerId())
                .questionId(row.getQuestionId())
                .assessmentTimeId(assessmentTimeId)
                .userId(row.getCandidateUserId())
                .score(row.getJudgementScore())
                .maxScore(row.getJudgementMaxScore())
                .status(row.getJudgementStatus())
                .resultCode(row.getResultCode())
                .source(row.getSource())
                .reviewerId(row.getReviewerId())
                .reviewerType(row.getReviewerType())
                .comment(row.getJudgementComment())
                .judgedAt(row.getJudgedAt())
                .build();
    }
}
