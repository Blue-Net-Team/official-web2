package com.bluenet.web.api.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量下载请求
 */
@Schema(description = "批量下载请求")
@Data
public class BatchDownloadRequestDTO {

    @Schema(description = "文件条目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文件条目列表不能为空")
    @Valid
    private List<BatchDownloadEntryDTO> entries;

    @Schema(description = "ZIP 包名称")
    private String zipName;
}
