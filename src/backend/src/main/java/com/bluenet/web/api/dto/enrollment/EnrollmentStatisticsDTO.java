package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "报名统计")
public class EnrollmentStatisticsDTO {
    @Schema(description = "总报名数", example = "100")
    private Long total;

    @Schema(description = "按状态统计")
    private Map<String, Long> byStatus;

    @Schema(description = "按方向统计")
    private Map<Direction, Long> byDirection;
}
