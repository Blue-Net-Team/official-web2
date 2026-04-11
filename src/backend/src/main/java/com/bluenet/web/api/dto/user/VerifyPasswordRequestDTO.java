package com.bluenet.web.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "验证当前密码请求")
@Data
public class VerifyPasswordRequestDTO {

    @Schema(description = "当前密码", example = "MyPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;
}
