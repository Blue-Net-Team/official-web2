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
@Schema(description = "批量添加/移除角色请求")
public class PermissionRoleBatchRequestDTO {
    @NotEmpty(message = "角色名称列表不能为空")
    @Schema(description = "角色名称列表", required = true, example = "[\"MEMBER\", \"CANDIDATE\"]")
    private List<String> roleNames;
}
