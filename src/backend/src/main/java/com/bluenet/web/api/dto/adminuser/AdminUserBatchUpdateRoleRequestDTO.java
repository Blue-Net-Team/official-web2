package com.bluenet.web.api.dto.adminuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "批量更新角色请求")
public class AdminUserBatchUpdateRoleRequestDTO {
    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 50, message = "一次最多操作50个用户")
    @Schema(description = "用户ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> userIds;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;
}
