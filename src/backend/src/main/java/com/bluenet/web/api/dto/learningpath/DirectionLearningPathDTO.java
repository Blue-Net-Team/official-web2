package com.bluenet.web.api.dto.learningpath;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 方向学习路径DTO
 * <p>
 * 用于API层返回完整的学习路径信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "方向学习路径")
public class DirectionLearningPathDTO {
    @Schema(description = "方向标识（slug）", example = "cv")
    private String direction;

    @Schema(description = "方向名称", example = "计算机视觉")
    private String directionName;

    @Schema(description = "学习步骤列表")
    private List<LearningStepDTO> steps;
}
