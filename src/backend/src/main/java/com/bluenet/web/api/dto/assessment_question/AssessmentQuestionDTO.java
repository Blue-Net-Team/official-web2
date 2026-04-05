package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 考题数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考题信息")
public class AssessmentQuestionDTO {
    @Schema(description = "考题ID")
    private Long id;

    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;

    @Schema(description = "题号")
    private Integer questionNo;

    @Schema(description = "题型")
    private QuestionType questionType;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容（仅管理端返回完整内容）")
    private Object content;

    @Schema(description = "附件ID")
    private Long attachmentId;

    @Schema(description = "分值")
    private BigDecimal score;

    @Schema(description = "当前用户是否已作答（仅用户端返回）")
    private Boolean answered;
}
