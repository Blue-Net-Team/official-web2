package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 知识库分段列表项响应 DTO。
 */
@Schema(description = "知识库分段列表项")
public record KnowledgeChunkListItemResponseDTO(
        @Schema(description = "分段ID") Long id,
        @Schema(description = "文档ID") Long docId,
        @Schema(description = "内容") String content,
        @Schema(description = "标签列表") List<String> tags,
        @Schema(description = "来源") String source) {
}
