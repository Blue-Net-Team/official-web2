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
@Schema(description = "权限角色操作结果")
public class PermissionRoleResponseDTO {
    @Schema(description = "操作成功的数量", example = "2")
    private int successCount;

    @Schema(description = "操作后该权限关联的全部角色名称列表")
    private List<String> currentRoles;
}
