package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain view of a question-level assessment judgement.
 */
@Data
@Builder
public class AssessmentJudgementVO {
    private Long id;
    private Long answerId;
    private Long questionId;
    private Long assessmentTimeId;
    private Long userId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private JudgementStatus status;
    private ObjectiveResultCode resultCode;
    private JudgementSource source;
    private Long reviewerId;
    private ReviewerType reviewerType;
    private String comment;
    private LocalDateTime judgedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
