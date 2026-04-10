package com.bluenet.web.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 密码重置 - 发送验证码请求。 */
@Schema(description = "密码重置 - 发送验证码请求")
@Data
public class SendResetCodeRequestDTO {

    @Schema(description = "重置流程令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "resetToken不能为空")
    private String resetToken;
}
