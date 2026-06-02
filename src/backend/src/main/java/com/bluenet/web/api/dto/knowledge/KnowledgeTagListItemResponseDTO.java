package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 知识库标签列表项响应 DTO。
 */
@Schema(description = "知识库标签列表项")
public record KnowledgeTagListItemResponseDTO(
        @Schema(description = "标签ID") Long id,
        @Schema(description = "标签名称") String tagName,
        @Schema(description = "标签描述") String tagDescription,
        @Schema(description = "关联分段数量") Integer chunksCount) {
}
