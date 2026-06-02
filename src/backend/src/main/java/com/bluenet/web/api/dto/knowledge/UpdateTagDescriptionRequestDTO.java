package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 更新标签描述请求 DTO。
 */
@Schema(description = "更新标签描述请求")
public record UpdateTagDescriptionRequestDTO(
        @NotBlank @Schema(description = "标签描述") String description) {
}
