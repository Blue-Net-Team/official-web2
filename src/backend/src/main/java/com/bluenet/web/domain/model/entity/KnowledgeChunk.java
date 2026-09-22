package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ChunkVectorStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识库分段领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeChunk {

    private Long id;
    private Long docId;
    private String content;
    private List<Long> tagIds;
    private String source;
    private ChunkVectorStatus vectorStatus;

    /**
     * 创建新分段。
     *
     * @param docId
     *            文档ID
     * @param content
     *            内容
     * @param tagIds
     *            标签ID列表
     * @param source
     *            来源
     * @return 新分段实体
     */
    public static KnowledgeChunk create(Long docId, String content, List<Long> tagIds, String source) {
        if (docId == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        return new KnowledgeChunk(null, docId,
                content != null ? content : "",
                tagIds != null ? tagIds : List.of(),
                source != null ? source : "",
                ChunkVectorStatus.SYNCED);
    }

    /**
     * 从数据库重建。
     */
    public static KnowledgeChunk reconstruct(Long id, Long docId, String content, List<Long> tagIds, String source,
            ChunkVectorStatus vectorStatus) {
        return new KnowledgeChunk(id, docId, content, tagIds, source,
                vectorStatus != null ? vectorStatus : ChunkVectorStatus.SYNCED);
    }

    /**
     * 更新内容与标签，并标记为待重新向量化。
     *
     * @param content
     *            新内容
     * @param tagIds
     *            新标签ID列表
     */
    public void updateContent(String content, List<Long> tagIds) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("分片内容不能为空");
        }
        this.content = content;
        this.tagIds = tagIds != null ? tagIds : List.of();
        this.vectorStatus = ChunkVectorStatus.EMBEDDING;
    }

    /**
     * 标记向量已同步（由 ai-service 重新向量化完成后调用）。
     */
    public void markVectorSynced() {
        this.vectorStatus = ChunkVectorStatus.SYNCED;
    }
}
