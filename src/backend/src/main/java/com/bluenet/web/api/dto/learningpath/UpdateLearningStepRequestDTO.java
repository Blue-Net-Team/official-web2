package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新学习步骤请求DTO
 * <p>
 * 用于管理员更新学习步骤信息。更新不改变展示顺序，请求不需要提供序号。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新学习步骤请求")
public class UpdateLearningStepRequestDTO {
    @NotBlank(message = "步骤标题不能为空")
    @Schema(description = "步骤标题", example = "Python基础", required = true)
    private String title;

    @Schema(description = "相关链接URL", example = "https://example.com/resource")
    private String relatedLink;
}
