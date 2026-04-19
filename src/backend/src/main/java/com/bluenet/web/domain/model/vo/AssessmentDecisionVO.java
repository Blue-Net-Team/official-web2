package com.bluenet.web.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain view of a candidate's final assessment decision.
 */
@Data
@Builder
public class AssessmentDecisionVO {
    private Long id;
    private Long userId;
    private Long assessmentTimeId;
    private Boolean passed;
    private Long decidedBy;
    private String decisionComment;
    private LocalDateTime decidedAt;
    private LocalDateTime updatedAt;
}
