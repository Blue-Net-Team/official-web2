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
        @Schema(description = "标签ID列表") List<Long> tagIds,
        @Schema(description = "来源") String source,
        @Schema(description = "向量同步状态：synced(已同步)、embedding(向量化中)") String vectorStatus) {
}
