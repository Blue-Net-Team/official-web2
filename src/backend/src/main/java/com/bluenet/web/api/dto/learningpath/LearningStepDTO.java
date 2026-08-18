package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习步骤DTO
 * <p>
 * 用于API层返回学习步骤信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "学习步骤信息")
public class LearningStepDTO {
    @Schema(description = "步骤ID")
    private Long id;

    @Schema(description = "步骤序号")
    private Integer stepNumber;

    @Schema(description = "步骤标题")
    private String title;

    @Schema(description = "相关链接URL")
    private String relatedLink;
}
