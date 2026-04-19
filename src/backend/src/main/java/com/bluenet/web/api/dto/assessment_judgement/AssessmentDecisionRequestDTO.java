package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for final candidate assessment decisions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核通过决策请求")
public class AssessmentDecisionRequestDTO {
    @Schema(description = "考生用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "考生用户ID不能为空")
    private Long userId;

    @Schema(description = "考核时间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "考核时间ID不能为空")
    private Long assessmentTimeId;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否通过不能为空")
    private Boolean passed;

    @Schema(description = "决策备注")
    private String decisionComment;
}
