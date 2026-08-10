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
    /** 考生报名时填写的内推码；未报名或未填写时为 null */
    private String internalReferralCode;
    /** 推荐人用户名；内推码为空或无效时为 null */
    private String referralUserName;
}
