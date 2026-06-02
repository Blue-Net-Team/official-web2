package com.bluenet.web.api.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传知识库文档请求 DTO。
 */
@Schema(description = "上传知识库文档请求")
public record UploadKnowledgeDocRequestDTO(
        @Schema(description = "文档标题，不传则使用文件名") String title,
        @NotNull @Schema(description = "Markdown 文件") MultipartFile file) {
}
