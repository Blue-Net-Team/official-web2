package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端考生维度评分汇总。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考生评分汇总")
public class AssessmentCandidateScoreboardDTO {
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
    @Schema(description = "各题评分状态")
    private List<AssessmentCandidateQuestionScoreDTO> questionScores;
    @Schema(description = "所属队伍ID")
    private Long teamId;
    @Schema(description = "所属队伍名称")
    private String teamName;
    @Schema(description = "是否为队长")
    private Boolean isLeader;
}
