package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 录用决策候选人行。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "录用决策候选人")
public class AssessmentDecisionCandidateDTO {
    @Schema(description = "考生用户ID")
    private Long candidateUserId;
    @Schema(description = "考生学号")
    private String studentId;
    @Schema(description = "考生姓名")
    private String username;
    @Schema(description = "考生昵称")
    private String nickname;
    @Schema(description = "总得分")
    private BigDecimal totalScore;
    @Schema(description = "总满分")
    private BigDecimal maxScore;
    @Schema(description = "已评分题数")
    private Long judgedQuestionCount;
    @Schema(description = "待评分题数")
    private Long pendingJudgementCount;
    @Schema(description = "决策ID")
    private Long decisionId;
    @Schema(description = "是否通过，null表示待决策")
    private Boolean passed;
    @Schema(description = "决策备注")
    private String decisionComment;
    @Schema(description = "决策人ID")
    private Long decidedBy;
    @Schema(description = "决策时间")
    private LocalDateTime decidedAt;
    @Schema(description = "各题评分状态")
    private List<AssessmentCandidateQuestionScoreDTO> questionScores;
}
