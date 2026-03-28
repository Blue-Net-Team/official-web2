package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建学习步骤请求DTO
 * <p>
 * 用于管理员创建新的学习步骤
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建学习步骤请求")
public class CreateLearningStepRequestDTO {
    @NotNull(message = "步骤序号不能为空")
    @Min(value = 1, message = "步骤序号最小为1")
    @Max(value = 100, message = "步骤序号最大为100")
    @Schema(description = "步骤序号", example = "1", required = true)
    private Integer stepNumber;

    @NotBlank(message = "步骤标题不能为空")
    @Schema(description = "步骤标题", example = "Python基础", required = true)
    private String title;

    @Schema(description = "视频链接URL", example = "https://example.com/video.mp4")
    private String videoUrl;
}
