package com.bluenet.web.domain.model.entity;

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
    private List<String> tags;
    private String source;

    /**
     * 创建新分段。
     *
     * @param docId
     *            文档ID
     * @param content
     *            内容
     * @param tags
     *            标签列表
     * @param source
     *            来源
     * @return 新分段实体
     */
    public static KnowledgeChunk create(Long docId, String content, List<String> tags, String source) {
        if (docId == null) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        return new KnowledgeChunk(null, docId,
                content != null ? content : "",
                tags != null ? tags : List.of(),
                source != null ? source : "");
    }

    /**
     * 从数据库重建。
     */
    public static KnowledgeChunk reconstruct(Long id, Long docId, String content, List<String> tags, String source) {
        return new KnowledgeChunk(id, docId, content, tags, source);
    }
}
