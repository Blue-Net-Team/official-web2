package com.bluenet.web.api.dto.assessment_judgement;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API response for a question-level judgement.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "题目评判结果")
public class AssessmentJudgementDTO {
    @Schema(description = "评判ID")
    private Long id;
    @Schema(description = "答案ID")
    private Long answerId;
    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;
    @Schema(description = "考生用户ID")
    private Long userId;
    @Schema(description = "得分")
    private BigDecimal score;
    @Schema(description = "满分")
    private BigDecimal maxScore;
    @Schema(description = "评判状态")
    private JudgementStatus status;
    @Schema(description = "客观题结果码")
    private ObjectiveResultCode resultCode;
    @Schema(description = "评判来源")
    private JudgementSource source;
    @Schema(description = "评判人ID")
    private Long reviewerId;
    @Schema(description = "评判人类型")
    private ReviewerType reviewerType;
    @Schema(description = "评判评论")
    private String comment;
    @Schema(description = "完成评判时间")
    private LocalDateTime judgedAt;
}
