package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库文档领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeDoc {

    private Long id;
    private Long fileId;
    private String title;
    private DocParseStatus status;
    private Integer chunkCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建新文档记录。
     *
     * @param fileId
     *            关联文件ID
     * @param title
     *            文档标题
     * @return 新文档实体
     */
    public static KnowledgeDoc create(Long fileId, String title) {
        if (fileId == null) {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        return new KnowledgeDoc(null, fileId, title != null ? title : "", DocParseStatus.PENDING, 0, "", now, now);
    }

    /**
     * 从数据库重建。
     */
    public static KnowledgeDoc reconstruct(Long id, Long fileId, String title, DocParseStatus status,
            Integer chunkCount, String errorMessage,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new KnowledgeDoc(id, fileId, title, status, chunkCount, errorMessage, createdAt, updatedAt);
    }

    /**
     * 标记为重新解析。
     */
    public void markForReparse() {
        this.status = DocParseStatus.PENDING;
        this.errorMessage = "";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新解析状态。
     *
     * @param status
     *            新状态
     * @param chunkCount
     *            分段数量
     * @param errorMessage
     *            错误信息
     */
    public void updateStatus(DocParseStatus status, Integer chunkCount, String errorMessage) {
        this.status = status;
        if (chunkCount != null) {
            this.chunkCount = chunkCount;
        }
        if (errorMessage != null) {
            this.errorMessage = errorMessage;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
