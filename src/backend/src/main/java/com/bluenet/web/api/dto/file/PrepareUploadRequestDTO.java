package com.bluenet.web.api.dto.file;

import com.bluenet.web.domain.model.enumerate.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预签名上传准备请求
 */
@Schema(description = "预签名上传准备请求")
@Data
public class PrepareUploadRequestDTO {

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原始文件名不能为空")
    private String filename;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件类型不能为空")
    private FileType type;

    @Schema(description = "文件大小（字节）", requiredMode = Schema.RequiredMode.REQUIRED)
    private long size;

    @Schema(description = "文件 Content-Type", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content-Type 不能为空")
    private String contentType;
}
