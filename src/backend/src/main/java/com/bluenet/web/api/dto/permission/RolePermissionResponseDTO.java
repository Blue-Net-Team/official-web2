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
@Schema(description = "角色权限操作结果")
public class RolePermissionResponseDTO {
    @Schema(description = "操作成功的数量", example = "5")
    private int successCount;

    @Schema(description = "操作后该角色拥有的全部权限标识符列表")
    private List<String> currentPermissions;
}
