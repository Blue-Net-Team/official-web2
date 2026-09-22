package com.bluenet.web.application.result.knowledge;

/**
 * 知识库分段应用层结果对象。
 */
public class KnowledgeChunkResult {

    private KnowledgeChunkResult() {
    }

    /**
     * 分段列表项。
     */
    public record ListItem(
            Long id,
            Long docId,
            String content,
            java.util.List<Long> tagIds,
            String source,
            String vectorStatus) {
    }
}
