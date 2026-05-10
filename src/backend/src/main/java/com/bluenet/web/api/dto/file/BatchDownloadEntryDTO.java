package com.bluenet.web.api.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量下载条目
 */
@Schema(description = "批量下载条目")
@Data
public class BatchDownloadEntryDTO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "自定义文件名（可不含扩展名，后端自动补全）")
    private String filename;
}
