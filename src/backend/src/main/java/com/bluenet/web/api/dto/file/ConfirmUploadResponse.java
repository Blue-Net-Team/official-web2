package com.bluenet.web.api.dto.file;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 预签名上传确认响应
 */
@Data
@AllArgsConstructor
@Builder
@Schema(description = "预签名上传确认响应")
public class ConfirmUploadResponse {

    @Schema(description = "文件 ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件类型")
    private FileType type;

    @Schema(description = "文件状态")
    private FileStatus status;
}
