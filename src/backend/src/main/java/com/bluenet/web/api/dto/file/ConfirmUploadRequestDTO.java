package com.bluenet.web.api.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预签名上传确认请求
 */
@Schema(description = "预签名上传确认请求")
@Data
public class ConfirmUploadRequestDTO {

    @Schema(description = "文件 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "回调令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回调令牌不能为空")
    private String callbackToken;

    @Schema(description = "文件 MD5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件MD5不能为空")
    private String md5;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED)
    private long size;
}
