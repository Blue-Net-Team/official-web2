package com.bluenet.web.api.dto.file;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "文件信息")
public class FileInfo {
    @Schema(description = "文件ID")
    private Long id;
    @Schema(description = "文件名称")
    private String name;
    @Schema(description = "文件类型")
    private FileType type;
    @Schema(description = "已废弃，文件下载请使用 /api/v1/file/download/{id} 接口", deprecated = true)
    private String url;
    @Schema(description = "文件状态")
    private FileStatus status;
}
