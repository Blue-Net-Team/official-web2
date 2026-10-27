package com.bluenet.web.api.dto.assessment_question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "克隆考题请求")
public class CloneQuestionRequestDTO {
    @Schema(description = "目标考核时间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标考核时间ID不能为空")
    private Long targetAssessmentTimeId;

    @Schema(description = "目标题号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标题号不能为空")
    @Min(value = 1, message = "目标题号必须大于0")
    private Integer targetQuestionNo;
}
