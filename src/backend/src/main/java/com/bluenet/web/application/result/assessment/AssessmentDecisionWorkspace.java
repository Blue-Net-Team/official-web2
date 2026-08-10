package com.bluenet.web.application.result.assessment;

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
public class AssessmentDecisionWorkspace {
    private AssessmentDecisionStatistics statistics;
    private List<AssessmentDecisionCandidate> candidates;
}
