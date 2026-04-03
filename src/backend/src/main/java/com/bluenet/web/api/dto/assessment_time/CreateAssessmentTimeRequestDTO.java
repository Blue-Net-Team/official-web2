package com.bluenet.web.api.dto.assessment_time;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建考核时间请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建考核时间请求")
public class CreateAssessmentTimeRequestDTO {
    @NotNull(message = "方向不能为空")
    @Schema(description = "方向", required = true)
    private Direction direction;

    @NotNull(message = "届次不能为空")
    @Min(value = 1, message = "届次必须大于0")
    @Schema(description = "届次", required = true, example = "1")
    private Integer epoch;

    @NotNull(message = "年级不能为空")
    @Min(value = 1, message = "年级必须为1-3")
    @Max(value = 3, message = "年级必须为1-3")
    @Schema(description = "年级（1=大一, 2=大二, 3=大三）", required = true, example = "1")
    private Integer grade;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalDateTime endTime;

    @NotNull(message = "是否限时不能为空")
    @Schema(description = "是否限制时间", required = true)
    private Boolean timeLimit;

    @Schema(description = "限时分钟数（timeLimit为true时必填）")
    private Integer timeLimitMinutes;
}
