package com.bluenet.web.api.converter.assessment_judgement;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateQuestionScoreDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionCandidateDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionStatisticsDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionWorkspaceDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionHistoryDTO;
import com.bluenet.web.application.AssessmentDecisionResult;
import com.bluenet.web.application.AssessmentJudgementResult;
import com.bluenet.web.domain.model.vo.AssessmentCandidateQuestionScoreVO;
import com.bluenet.web.domain.model.vo.AssessmentCandidateScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionCandidateVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionStatisticsVO;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import com.bluenet.web.domain.model.vo.AssessmentDecisionWorkspaceVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionScoreboardVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionHistoryVO;
import com.bluenet.web.domain.model.vo.AssessmentQuestionSubmissionVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 考核评判响应转换器
 * <p>
 * 负责将评判、提交、评分相关 Result/VO 转换为接口 DTO
 * </p>
 */
@Component
public class AssessmentJudgementResponseConverter {

    /**
     * 将评判应用层结果转换为接口 DTO
     */
    public AssessmentJudgementDTO toDTO(AssessmentJudgementResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentJudgementDTO.builder()
                .id(result.id())
                .answerId(result.answerId())
                .questionId(result.questionId())
                .assessmentTimeId(result.assessmentTimeId())
                .userId(result.userId())
                .score(result.score())
                .maxScore(result.maxScore())
                .status(result.status())
                .resultCode(result.resultCode())
                .source(result.source())
                .reviewerId(result.reviewerId())
                .reviewerType(result.reviewerType())
                .judgedAt(result.judgedAt())
                .build();
    }

    /**
     * 将决策应用层结果转换为接口 DTO
     */
    public AssessmentDecisionDTO toDTO(AssessmentDecisionResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentDecisionDTO.builder()
                .id(result.id())
                .userId(result.userId())
                .assessmentTimeId(result.assessmentTimeId())
                .passed(result.passed())
                .decidedBy(result.decidedBy())
                .decisionComment(result.decisionComment())
                .decidedAt(result.decidedAt())
                .build();
    }

    /**
     * 将题目评分汇总 VO 转换为接口 DTO
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
     * 将题目提交 VO 转换为接口 DTO
     */
    public AssessmentQuestionSubmissionDTO convertSubmissionToDTO(AssessmentQuestionSubmissionVO vo) {
        List<AssessmentQuestionSubmissionHistoryDTO> histories = vo.getHistories() == null
                ? List.of()
                : vo.getHistories()
                        .stream()
                        .map(this::convertSubmissionHistoryToDTO)
                        .toList();
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
                .histories(histories)
                .teamId(vo.getTeamId())
                .teamName(vo.getTeamName())
                .isLeader(vo.getIsLeader())
                .build();
    }

    /**
     * 将评判历史 VO 转换为接口 DTO
     */
    public AssessmentQuestionSubmissionHistoryDTO convertSubmissionHistoryToDTO(
            AssessmentQuestionSubmissionHistoryVO vo) {
        return AssessmentQuestionSubmissionHistoryDTO.builder()
                .judgement(convertToDTO(vo.getJudgement()))
                .selectedBest(Boolean.TRUE.equals(vo.getSelectedBest()))
                .build();
    }

    /**
     * 将考生评分汇总 VO 转换为接口 DTO
     */
    public AssessmentCandidateScoreboardDTO convertCandidateScoreboardToDTO(AssessmentCandidateScoreboardVO vo) {
        return AssessmentCandidateScoreboardDTO.builder()
                .candidateUserId(vo.getCandidateUserId())
                .studentId(vo.getStudentId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .totalScore(vo.getTotalScore())
                .maxScore(vo.getMaxScore())
                .judgedQuestionCount(vo.getJudgedQuestionCount())
                .pendingJudgementCount(vo.getPendingJudgementCount())
                .questionScores(
                        vo.getQuestionScores() == null
                                ? List.of()
                                : vo.getQuestionScores()
                                        .stream()
                                        .map(this::convertQuestionScoreToDTO)
                                        .toList())
                .teamId(vo.getTeamId())
                .teamName(vo.getTeamName())
                .isLeader(vo.getIsLeader())
                .build();
    }

    /**
     * 将录用决策工作台 VO 转换为接口 DTO
     */
    public AssessmentDecisionWorkspaceDTO convertDecisionWorkspaceToDTO(AssessmentDecisionWorkspaceVO vo) {
        return AssessmentDecisionWorkspaceDTO.builder()
                .statistics(convertDecisionStatisticsToDTO(vo.getStatistics()))
                .candidates(
                        vo.getCandidates() == null
                                ? List.of()
                                : vo.getCandidates()
                                        .stream()
                                        .map(this::convertDecisionCandidateToDTO)
                                        .toList())
                .build();
    }

    /**
     * 将录用决策统计 VO 转换为接口 DTO
     */
    public AssessmentDecisionStatisticsDTO convertDecisionStatisticsToDTO(AssessmentDecisionStatisticsVO vo) {
        return AssessmentDecisionStatisticsDTO.builder()
                .candidates(vo.getCandidates())
                .pending(vo.getPending())
                .passed(vo.getPassed())
                .eliminated(vo.getEliminated())
                .build();
    }

    /**
     * 将录用决策候选人 VO 转换为接口 DTO
     */
    public AssessmentDecisionCandidateDTO convertDecisionCandidateToDTO(AssessmentDecisionCandidateVO vo) {
        return AssessmentDecisionCandidateDTO.builder()
                .candidateUserId(vo.getCandidateUserId())
                .studentId(vo.getStudentId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .totalScore(vo.getTotalScore())
                .maxScore(vo.getMaxScore())
                .judgedQuestionCount(vo.getJudgedQuestionCount())
                .pendingJudgementCount(vo.getPendingJudgementCount())
                .decisionId(vo.getDecisionId())
                .passed(vo.getPassed())
                .decisionComment(vo.getDecisionComment())
                .decidedBy(vo.getDecidedBy())
                .decidedAt(vo.getDecidedAt())
                .questionScores(
                        vo.getQuestionScores() == null
                                ? List.of()
                                : vo.getQuestionScores()
                                        .stream()
                                        .map(this::convertQuestionScoreToDTO)
                                        .toList())
                .teamId(vo.getTeamId())
                .teamName(vo.getTeamName())
                .isLeader(vo.getIsLeader())
                .build();
    }

    /**
     * 将考生单题评分 VO 转换为接口 DTO
     */
    public AssessmentCandidateQuestionScoreDTO convertQuestionScoreToDTO(AssessmentCandidateQuestionScoreVO vo) {
        return AssessmentCandidateQuestionScoreDTO.builder()
                .questionId(vo.getQuestionId())
                .questionNo(vo.getQuestionNo())
                .questionTitle(vo.getQuestionTitle())
                .questionType(vo.getQuestionType())
                .maxScore(vo.getMaxScore())
                .answerId(vo.getAnswerId())
                .submitted(vo.getSubmitted())
                .submitTime(vo.getSubmitTime())
                .score(vo.getScore())
                .judged(vo.getJudged())
                .latestJudgement(convertToDTO(vo.getLatestJudgement()))
                .build();
    }

    /**
     * 将评判实体转换为接口 DTO
     */
    public AssessmentJudgementDTO convertToDTO(AssessmentJudgement judgement) {
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
                .judgedAt(judgement.getJudgedAt())
                .build();
    }

    /**
     * 将决策 VO 转换为接口 DTO
     */
    public AssessmentDecisionDTO convertToDTO(AssessmentDecisionVO decision) {
        if (decision == null) {
            return null;
        }
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
                .judgedAt(vo.getJudgedAt())
                .build();
    }
}
