package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * KnowledgeChunk 领域实体单元测试。
 */
@DisplayName("KnowledgeChunk 领域实体测试")
class KnowledgeChunkTest {

    @Test
    @DisplayName("create: 应创建新分段")
    void create_shouldCreateChunk() {
        List<String> tags = List.of("tag1", "tag2");
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, "内容", tags, "来源");

        assertThat(chunk.getId()).isNull();
        assertThat(chunk.getDocId()).isEqualTo(1L);
        assertThat(chunk.getContent()).isEqualTo("内容");
        assertThat(chunk.getTags()).isEqualTo(tags);
        assertThat(chunk.getSource()).isEqualTo("来源");
    }

    @Test
    @DisplayName("create: 文档ID为空应抛异常")
    void create_withNullDocId_shouldThrow() {
        assertThatThrownBy(() -> KnowledgeChunk.create(null, "内容", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文档ID不能为空");
    }

    @Test
    @DisplayName("create: 空字段应使用默认值")
    void create_withNullFields_shouldUseDefaults() {
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, null, null, null);

        assertThat(chunk.getContent()).isEqualTo("");
        assertThat(chunk.getTags()).isEmpty();
        assertThat(chunk.getSource()).isEqualTo("");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        List<String> tags = List.of("tag");
        KnowledgeChunk chunk = KnowledgeChunk.reconstruct(10L, 2L, "内容", tags, "来源");

        assertThat(chunk.getId()).isEqualTo(10L);
        assertThat(chunk.getDocId()).isEqualTo(2L);
        assertThat(chunk.getContent()).isEqualTo("内容");
        assertThat(chunk.getTags()).isEqualTo(tags);
        assertThat(chunk.getSource()).isEqualTo("来源");
    }
}
