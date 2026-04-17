package com.bluenet.web.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "批量分配/移除权限请求")
public class RolePermissionBatchRequestDTO {
    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", required = true, example = "[1, 2, 3]")
    private List<Long> permissionIds;
}
