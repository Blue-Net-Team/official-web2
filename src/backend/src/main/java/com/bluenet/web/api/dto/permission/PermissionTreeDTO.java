package com.bluenet.web.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "权限树节点DTO")
public class PermissionTreeDTO {
    @Schema(description = "节点唯一标识（权限ID或路径）", example = "1 或 assessment:create")
    private String key;

    @Schema(description = "节点显示标题", example = "assessment")
    private String title;

    @Schema(description = "权限标识符（仅叶子节点有）", example = "assessment:create")
    private String value;

    @Schema(description = "权限ID（仅叶子节点有）", example = "1")
    private Long permissionId;

    @Schema(description = "是否为叶子节点（true=权限，false=目录）")
    private boolean leaf;

    @Schema(description = "子节点列表")
    @Builder.Default
    private List<PermissionTreeDTO> children = List.of();

    @Schema(description = "访问级别: PUBLIC/AUTHENTICATED/PROTECTED（仅叶子节点有值）")
    private String accessLevel;

    @Schema(description = "该节点下叶子节点（权限）数量", example = "5")
    private Integer permissionCount;
}
