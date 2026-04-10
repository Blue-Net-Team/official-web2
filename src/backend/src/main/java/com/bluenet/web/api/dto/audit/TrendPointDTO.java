package com.bluenet.web.api.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "请求量趋势数据点")
public class TrendPointDTO {
    @Schema(description = "时间桶起始时间")
    private LocalDateTime time;
    @Schema(description = "该时间段内的请求数量")
    private Long count;
}
