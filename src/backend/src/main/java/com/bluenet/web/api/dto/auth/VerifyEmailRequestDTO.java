package com.bluenet.web.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 密码重置 - 验证邮箱请求。 */
@Schema(description = "密码重置 - 验证邮箱请求")
@Data
public class VerifyEmailRequestDTO {

    @Schema(description = "重置流程令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "resetToken不能为空")
    private String resetToken;

    @Schema(description = "邮箱地址", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String email;
}
