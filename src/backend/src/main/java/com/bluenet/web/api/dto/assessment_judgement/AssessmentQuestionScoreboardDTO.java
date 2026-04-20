package com.bluenet.web.api.dto.assessment_judgement;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理端题目评分汇总行。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "题目评分汇总")
public class AssessmentQuestionScoreboardDTO {
    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;
    @Schema(description = "题号")
    private Integer questionNo;
    @Schema(description = "题型")
    private QuestionType questionType;
    @Schema(description = "题目标题")
    private String title;
    @Schema(description = "满分")
    private BigDecimal maxScore;
    @Schema(description = "提交数")
    private Long submittedCount;
    @Schema(description = "已评分数")
    private Long judgedCount;
    @Schema(description = "待评分数")
    private Long pendingCount;
    @Schema(description = "平均分")
    private BigDecimal averageScore;
}
