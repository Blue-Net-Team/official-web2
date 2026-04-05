package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建考题请求")
public class CreateQuestionRequestDTO {
    @Schema(description = "考核时间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "考核时间ID不能为空")
    private Long assessmentTimeId;

    @Schema(description = "题号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题号不能为空")
    private Integer questionNo;

    @Schema(description = "题型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题型不能为空")
    private QuestionType questionType;

    @Schema(description = "题目标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "题目标题不能为空")
    private String title;

    @Schema(description = "题目内容（JSON格式）")
    private Object content;

    @Schema(description = "附件ID")
    private Long attachmentId;

    @Schema(description = "分值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分值不能为空")
    private BigDecimal score;
}
