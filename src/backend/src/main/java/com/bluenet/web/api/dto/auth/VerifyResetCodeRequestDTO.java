package com.bluenet.web.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 密码重置 - 验证验证码请求。 */
@Schema(description = "密码重置 - 验证验证码请求")
@Data
public class VerifyResetCodeRequestDTO {

    @Schema(description = "重置流程令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "resetToken不能为空")
    private String resetToken;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String code;
}
