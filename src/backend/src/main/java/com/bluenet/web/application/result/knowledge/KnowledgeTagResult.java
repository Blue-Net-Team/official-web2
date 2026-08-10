package com.bluenet.web.application.result.knowledge;

/**
 * 知识库标签应用层结果对象。
 */
public class KnowledgeTagResult {

    private KnowledgeTagResult() {
    }

    /**
     * 标签列表项。
     */
    public record ListItem(
            Long id,
            String tagName,
            String tagDescription,
            Integer chunksCount) {
    }
}
