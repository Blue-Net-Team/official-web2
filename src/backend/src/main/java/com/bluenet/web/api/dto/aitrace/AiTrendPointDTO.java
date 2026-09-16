package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "会话量趋势数据点")
public class AiTrendPointDTO {
    @Schema(description = "时间桶起始时间")
    private LocalDateTime time;
    @Schema(description = "该时间桶内新增的会话数量")
    private Long count;
}
