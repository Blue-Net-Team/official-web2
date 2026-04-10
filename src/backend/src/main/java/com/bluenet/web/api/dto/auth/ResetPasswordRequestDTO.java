package com.bluenet.web.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 密码重置 - 重置密码请求。 */
@Schema(description = "密码重置 - 重置密码请求")
@Data
public class ResetPasswordRequestDTO {

    @Schema(description = "重置流程令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "resetToken不能为空")
    private String resetToken;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String newPassword;

    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
