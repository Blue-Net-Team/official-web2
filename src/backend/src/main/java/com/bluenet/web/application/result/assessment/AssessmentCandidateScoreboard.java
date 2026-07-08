package com.bluenet.web.application.result.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考生维度评分汇总视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentCandidateScoreboard {
    private Long candidateUserId;
    private String studentId;
    private String username;
    private String nickname;
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Long judgedQuestionCount;
    private Long pendingJudgementCount;
    private List<AssessmentCandidateQuestionScore> questionScores;
    private Long teamId;
    private String teamName;
    private Boolean isLeader;
}
