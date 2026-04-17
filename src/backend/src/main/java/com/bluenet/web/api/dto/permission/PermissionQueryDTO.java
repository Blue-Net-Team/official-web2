package com.bluenet.web.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "权限查询参数")
public class PermissionQueryDTO {
    @Schema(description = "页码，从0开始", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "每页数量，默认20，最大100", example = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "关键词搜索（权限标识符/名称）", example = "assessment")
    private String keyword;

    @Schema(description = "权限格式筛选：resource:action / resource:subresource:action / resource-action:action", example = "resource:action")
    private String format;
}
