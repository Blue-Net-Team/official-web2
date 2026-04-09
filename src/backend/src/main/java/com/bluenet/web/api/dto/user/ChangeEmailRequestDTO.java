package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "修改邮箱请求")
@Data
public class ChangeEmailRequestDTO {

    @Schema(description = "原邮箱验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原邮箱验证码不能为空")
    private String originalEmailVerifyCode;

    @Schema(description = "新邮箱", example = "new@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String newEmail;

    @Schema(description = "新邮箱验证码", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新邮箱验证码不能为空")
    private String newEmailVerifyCode;
}
