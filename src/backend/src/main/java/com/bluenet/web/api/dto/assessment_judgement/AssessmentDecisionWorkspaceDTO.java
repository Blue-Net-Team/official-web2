package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 录用决策工作台响应。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "录用决策工作台")
public class AssessmentDecisionWorkspaceDTO {
    @Schema(description = "统计数据")
    private AssessmentDecisionStatisticsDTO statistics;
    @Schema(description = "候选人列表")
    private List<AssessmentDecisionCandidateDTO> candidates;
}
