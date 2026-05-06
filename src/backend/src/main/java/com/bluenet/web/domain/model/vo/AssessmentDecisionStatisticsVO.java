package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 录用决策统计视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDecisionStatisticsVO {
    private Long candidates;
    private Long pending;
    private Long passed;
    private Long eliminated;
}
