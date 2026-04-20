package com.bluenet.web.domain.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * Domain view of a single judgement history record for a submission.
 */
@Data
@Builder
public class AssessmentQuestionSubmissionHistoryVO {
    private AssessmentJudgementVO judgement;
    private Boolean selectedBest;
}
