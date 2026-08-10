package com.bluenet.web.application.result.knowledge;

import com.bluenet.web.domain.model.enumerate.DocParseStatus;

import java.time.LocalDateTime;

/**
 * 知识库文档应用层结果对象。
 */
public class KnowledgeDocResult {

    private KnowledgeDocResult() {
    }

    /**
     * 文档列表项。
     */
    public record ListItem(
            Long id,
            Long fileId,
            String title,
            DocParseStatus status,
            Integer chunkCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }

    /**
     * 文档详情。
     */
    public record Detail(
            Long id,
            Long fileId,
            String title,
            DocParseStatus status,
            Integer chunkCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }

    /**
     * 上传结果。
     */
    public record Uploaded(
            Long docId,
            DocParseStatus status) {
    }
}
