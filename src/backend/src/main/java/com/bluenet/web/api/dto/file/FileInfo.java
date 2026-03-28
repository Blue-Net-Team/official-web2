package com.bluenet.web.api.dto.file;

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
    @Schema(description = "文件url，不包含控制层前缀")
    private String url;
}
