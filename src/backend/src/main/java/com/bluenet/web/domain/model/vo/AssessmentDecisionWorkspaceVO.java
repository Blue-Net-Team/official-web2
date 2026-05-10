package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 录用决策工作台视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDecisionWorkspaceVO {
    private AssessmentDecisionStatisticsVO statistics;
    private List<AssessmentDecisionCandidateVO> candidates;
}
