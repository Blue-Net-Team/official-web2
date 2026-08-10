package com.bluenet.web.domain.model.readmodel;

import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import lombok.Builder;
import lombok.Data;

/**
 * Domain view of a single judgement history record for a submission.
 */
@Data
@Builder
public class AssessmentQuestionSubmissionHistoryReadModel {
    /**
     * 当前提交对应的评审结果实体。
     */
    private AssessmentJudgement judgement;
    /**
     * 该提交是否被选为当前题目的最佳或最终采用提交。
     */
    private Boolean selectedBest;
}
