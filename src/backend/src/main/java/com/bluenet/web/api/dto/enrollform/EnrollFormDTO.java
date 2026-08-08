package com.bluenet.web.api.dto.enrollform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报名表 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前报名表信息")
public class EnrollFormDTO {

    @Schema(description = "文件ID，用于下载报名表")
    private Long fileId;

    @Schema(description = "上传时间")
    private LocalDateTime createdAt;
}
