package com.bluenet.web.api.dto.health;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 健康检查状态响应 DTO。
 */
@Schema(description = "健康检查状态响应")
@Data
@Builder
public class HealthStatusDTO {

    @Schema(description = "整体状态", example = "UP")
    private String status;

    @Schema(description = "各组件状态")
    private Map<String, ComponentHealth> components;

    /**
     * 组件健康状态。
     */
    @Schema(description = "组件健康状态")
    @Data
    @Builder
    public static class ComponentHealth {

        @Schema(description = "组件状态", example = "UP")
        private String status;

        @Schema(description = "错误信息（仅当状态为 DOWN 时）")
        private String error;
    }
}
