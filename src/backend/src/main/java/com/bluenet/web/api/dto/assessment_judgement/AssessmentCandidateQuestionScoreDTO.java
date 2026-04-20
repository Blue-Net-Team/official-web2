package com.bluenet.web.api.dto.assessment_judgement;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考生在单题上的提交与评分状态。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考生单题评分状态")
public class AssessmentCandidateQuestionScoreDTO {
    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "题号")
    private Integer questionNo;
    @Schema(description = "题目标题")
    private String questionTitle;
    @Schema(description = "题型")
    private QuestionType questionType;
    @Schema(description = "题目满分")
    private BigDecimal maxScore;
    @Schema(description = "答案ID")
    private Long answerId;
    @Schema(description = "是否已提交")
    private Boolean submitted;
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;
    @Schema(description = "最新得分")
    private BigDecimal score;
    @Schema(description = "是否已评分")
    private Boolean judged;
    @Schema(description = "最新评判")
    private AssessmentJudgementDTO latestJudgement;
}
