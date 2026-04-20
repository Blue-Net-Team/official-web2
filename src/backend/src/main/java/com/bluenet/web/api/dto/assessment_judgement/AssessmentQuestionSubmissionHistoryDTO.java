package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端题目提交的历史评判记录。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "题目提交历史评判记录")
public class AssessmentQuestionSubmissionHistoryDTO {
    @Schema(description = "本次历史评判")
    private AssessmentJudgementDTO judgement;
    @Schema(description = "是否为当前展示用的最佳/最新记录")
    private Boolean selectedBest;
}
