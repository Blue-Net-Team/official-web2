package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 更新知识库标签请求 DTO（字段为 null 表示不修改）。
 */
@Schema(description = "更新知识库标签请求")
public record UpdateTagRequestDTO(
        @Schema(description = "新标签名（null 表示不修改）") @Size(max = 128, message = "标签名不能超过128字符") String tagName,

        @Schema(description = "新标签描述（null 表示不修改）") @Size(max = 512, message = "标签描述不能超过512字符") String description) {
}
