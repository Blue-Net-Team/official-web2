package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 录用决策候选人视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDecisionCandidateVO {
    private Long candidateUserId;
    private String studentId;
    private String username;
    private String nickname;
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Long judgedQuestionCount;
    private Long pendingJudgementCount;
    private Long decisionId;
    private Boolean passed;
    private String decisionComment;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private List<AssessmentCandidateQuestionScoreVO> questionScores;
    private Long teamId;
    private String teamName;
    private Boolean isLeader;
}
