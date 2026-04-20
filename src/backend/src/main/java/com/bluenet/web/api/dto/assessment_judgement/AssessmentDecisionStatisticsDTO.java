package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 录用决策统计。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "录用决策统计")
public class AssessmentDecisionStatisticsDTO {
    @Schema(description = "候选人数")
    private Long candidates;
    @Schema(description = "待决策人数")
    private Long pending;
    @Schema(description = "通过人数")
    private Long passed;
    @Schema(description = "淘汰人数")
    private Long eliminated;
}
