package com.bluenet.web.api.dto.adminuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建用户响应")
public class AdminUserCreateResponseDTO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "姓名")
    private String username;

    @Schema(description = "角色ID")
    private Long roleId;
}
