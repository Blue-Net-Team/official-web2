package com.bluenet.web.application.result.assessment;

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
public class AssessmentDecisionCandidate {
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
    private List<AssessmentCandidateQuestionScore> questionScores;
    private Long teamId;
    private String teamName;
    private Boolean isLeader;
    /** 考生报名时填写的内推码；未报名或未填写时为 null */
    private String internalReferralCode;
    /** 推荐人用户名；内推码为空或无效时为 null */
    private String referralUserName;

    /**
     * 是否为内推考生（内推码匹配到了真实成员）。
     * <p>
     * 仅填写了内推码但码无效（未匹配到成员）时不视为内推，不展示也不参与置顶排序。
     * </p>
     *
     * @return 内推码有效时返回 true。
     */
    public boolean isReferred() {
        return referralUserName != null && !referralUserName.isBlank();
    }
}
