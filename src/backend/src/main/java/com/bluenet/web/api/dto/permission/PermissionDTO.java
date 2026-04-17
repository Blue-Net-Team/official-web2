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
@Schema(description = "权限详情DTO")
public class PermissionDTO {
    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限标识符", example = "assessment:create")
    private String value;

    @Schema(description = "权限显示名称", example = "创建考核")
    private String name;

    @Schema(description = "接口URL", example = "/api/v1/admin/assessments")
    private String url;

    @Schema(description = "HTTP方法", example = "POST")
    private String method;

    @Schema(description = "已分配该权限的角色名列表")
    @Builder.Default
    private List<String> assignedRoles = List.of();
}
