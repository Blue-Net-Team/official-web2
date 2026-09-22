package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 编辑知识库分段请求 DTO。
 */
@Schema(description = "编辑知识库分段请求")
public record UpdateChunkRequestDTO(
        @Schema(description = "分片内容") @NotBlank(message = "分片内容不能为空") String content,

        @Schema(description = "标签ID列表，全量替换该分片的标签") @NotNull(message = "标签列表不能为空") List<Long> tagIds) {
}
