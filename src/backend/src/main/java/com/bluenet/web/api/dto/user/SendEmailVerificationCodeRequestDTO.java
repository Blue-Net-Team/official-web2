package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "发送修改邮箱验证码请求")
@Data
public class SendEmailVerificationCodeRequestDTO {

    @Schema(description = "邮箱", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String email;

    @Schema(description = "验证码场景", example = "change-email-original", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "场景不能为空")
    private String scene;
}
