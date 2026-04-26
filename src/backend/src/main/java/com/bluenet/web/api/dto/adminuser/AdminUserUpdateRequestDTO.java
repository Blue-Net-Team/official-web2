package com.bluenet.web.api.dto.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新用户信息请求")
public class AdminUserUpdateRequestDTO {
    @Schema(description = "角色ID", example = "2")
    private Long roleId;

    @Schema(description = "方向", example = "computer_vision")
    private Direction direction;

    @Schema(description = "禁用状态", example = "true")
    private Boolean disable;

    @Schema(description = "岗位", example = "测试工程师")
    private String job;
}
