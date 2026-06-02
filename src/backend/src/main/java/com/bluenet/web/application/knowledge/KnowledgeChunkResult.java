package com.bluenet.web.application.knowledge;

import java.util.List;

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
            List<String> tags,
            String source) {
    }
}
