package com.bluenet.web.api.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口访问排名条目")
public class EndpointRankingDTO {
    @Schema(description = "URI 路径模板", example = "/api/v1/file/download/{fileId}")
    private String pattern;
    @Schema(description = "总请求数")
    private Long count;
    @Schema(description = "平均响应时间（毫秒）")
    private Double avgDurationMs;
    @Schema(description = "失败请求数")
    private Long errorCount;
}
