package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "修改密码请求")
@Data
public class ChangePasswordRequestDTO {

    @Schema(description = "验证令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证令牌不能为空")
    private String token;

    @Schema(description = "新密码（前端SHA-256哈希后的值）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @Schema(description = "确认新密码", example = "NewPassword456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
