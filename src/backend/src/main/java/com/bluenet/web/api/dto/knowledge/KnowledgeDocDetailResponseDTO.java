package com.bluenet.web.api.dto.knowledge;

import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 知识库文档详情响应 DTO。
 */
@Schema(description = "知识库文档详情")
public record KnowledgeDocDetailResponseDTO(
        @Schema(description = "文档ID") Long id,
        @Schema(description = "文件ID") Long fileId,
        @Schema(description = "文档标题") String title,
        @Schema(description = "解析状态") DocParseStatus status,
        @Schema(description = "分段数量") Integer chunkCount,
        @Schema(description = "错误信息") String errorMessage,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {
}
