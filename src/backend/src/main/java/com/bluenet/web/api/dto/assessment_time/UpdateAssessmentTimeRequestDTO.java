package com.bluenet.web.api.dto.assessment_time;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新考核时间请求DTO
 * <p>
 * 所有字段可选，仅更新提供的字段
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新考核时间请求")
public class UpdateAssessmentTimeRequestDTO {
    @Schema(description = "方向")
    private Direction direction;

    @Schema(description = "届次")
    private Integer epoch;

    @Schema(description = "入学年份（如 2024、2025）")
    private Integer grade;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否限制时间")
    private Boolean timeLimit;

    @Schema(description = "限时分钟数")
    private Integer timeLimitMinutes;
}
