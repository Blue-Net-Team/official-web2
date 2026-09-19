package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习步骤DTO
 * <p>
 * 用于API层返回学习步骤信息。不包含任何序号字段：展示编号由前端按数组位置派生， 数组顺序即展示顺序。
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

    @Schema(description = "步骤标题")
    private String title;

    @Schema(description = "相关链接URL")
    private String relatedLink;
}
