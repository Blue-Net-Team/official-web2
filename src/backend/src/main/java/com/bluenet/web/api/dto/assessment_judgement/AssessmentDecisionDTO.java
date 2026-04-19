package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API response for a candidate's final assessment decision.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核通过决策")
public class AssessmentDecisionDTO {
    @Schema(description = "决策ID")
    private Long id;
    @Schema(description = "考生用户ID")
    private Long userId;
    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;
    @Schema(description = "是否通过")
    private Boolean passed;
    @Schema(description = "决策管理员ID")
    private Long decidedBy;
    @Schema(description = "决策备注")
    private String decisionComment;
    @Schema(description = "决策时间")
    private LocalDateTime decidedAt;
}
