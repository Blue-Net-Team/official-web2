package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新建知识库标签请求 DTO。
 */
@Schema(description = "新建知识库标签请求")
public record CreateTagRequestDTO(
        @Schema(description = "标签名") @NotBlank(message = "标签名不能为空") @Size(max = 128, message = "标签名不能超过128字符") String tagName,

        @Schema(description = "标签描述") @Size(max = 512, message = "标签描述不能超过512字符") String description) {
}
