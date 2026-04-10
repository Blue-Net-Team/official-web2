package com.bluenet.web.api.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口响应时间排名条目")
public class EndpointLatencyDTO {
    @Schema(description = "URI 路径模板", example = "/api/v1/file/download/{fileId}")
    private String pattern;
    @Schema(description = "平均响应时间（毫秒）")
    private Double avgDurationMs;
    @Schema(description = "最大响应时间（毫秒）")
    private Long maxDurationMs;
    @Schema(description = "总请求数")
    private Long count;
}
