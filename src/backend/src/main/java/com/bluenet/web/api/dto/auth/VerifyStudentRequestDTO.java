package com.bluenet.web.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 密码重置 - 验证学号请求。 */
@Schema(description = "密码重置 - 验证学号请求")
@Data
public class VerifyStudentRequestDTO {

    @Schema(description = "学号", example = "2021001001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学号不能为空")
    private String studentId;
}
