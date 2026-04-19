package com.bluenet.web.api.dto.assessment_judgement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body for manually reviewing file-upload answers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "人工评判请求")
public class ManualReviewRequestDTO {
    @Schema(description = "答案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "答案ID不能为空")
    private Long answerId;

    @Schema(description = "得分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "得分不能为空")
    private BigDecimal score;

    @Schema(description = "评判评论")
    private String comment;
}
