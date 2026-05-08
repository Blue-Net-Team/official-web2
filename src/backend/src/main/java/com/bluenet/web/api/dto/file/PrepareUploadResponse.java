package com.bluenet.web.api.dto.file;

import com.bluenet.web.domain.model.enumerate.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 预签名上传准备响应
 */
@Data
@AllArgsConstructor
@Builder
@Schema(description = "预签名上传准备响应")
public class PrepareUploadResponse {

    @Schema(description = "文件 ID")
    private Long fileId;

    @Schema(description = "预签名上传 URL")
    private String uploadUrl;

    @Schema(description = "回调令牌")
    private String callbackToken;

    @Schema(description = "生成的文件名")
    private String filename;

    @Schema(description = "文件类型")
    private FileType type;
}
