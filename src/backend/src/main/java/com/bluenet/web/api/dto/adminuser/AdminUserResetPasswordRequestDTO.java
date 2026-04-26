package com.bluenet.web.api.dto.adminuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "重置密码请求")
public class AdminUserResetPasswordRequestDTO {
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "newPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "newPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
