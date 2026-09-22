package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ChunkVectorStatus;
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
        List<Long> tagIds = List.of(1L, 2L);
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, "内容", tagIds, "来源");

        assertThat(chunk.getId()).isNull();
        assertThat(chunk.getDocId()).isEqualTo(1L);
        assertThat(chunk.getContent()).isEqualTo("内容");
        assertThat(chunk.getTagIds()).isEqualTo(tagIds);
        assertThat(chunk.getSource()).isEqualTo("来源");
        assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.SYNCED);
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
        assertThat(chunk.getTagIds()).isEmpty();
        assertThat(chunk.getSource()).isEqualTo("");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        List<Long> tagIds = List.of(5L);
        KnowledgeChunk chunk = KnowledgeChunk.reconstruct(
                10L,
                2L,
                "内容",
                tagIds,
                "来源",
                ChunkVectorStatus.EMBEDDING);

        assertThat(chunk.getId()).isEqualTo(10L);
        assertThat(chunk.getDocId()).isEqualTo(2L);
        assertThat(chunk.getContent()).isEqualTo("内容");
        assertThat(chunk.getTagIds()).isEqualTo(tagIds);
        assertThat(chunk.getSource()).isEqualTo("来源");
        assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.EMBEDDING);
    }

    @Test
    @DisplayName("reconstruct: vectorStatus 为 null 时应默认为 synced")
    void reconstruct_withNullVectorStatus_shouldDefaultSynced() {
        KnowledgeChunk chunk = KnowledgeChunk.reconstruct(10L, 2L, "内容", List.of(), "来源", null);

        assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.SYNCED);
    }

    @Test
    @DisplayName("updateContent: 应更新内容标签并标记待向量化")
    void updateContent_shouldMarkEmbedding() {
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, "旧内容", List.of(1L), "来源");

        chunk.updateContent("新内容", List.of(2L, 3L));

        assertThat(chunk.getContent()).isEqualTo("新内容");
        assertThat(chunk.getTagIds()).containsExactly(2L, 3L);
        assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.EMBEDDING);
    }

    @Test
    @DisplayName("updateContent: 内容为空应抛异常")
    void updateContent_withBlankContent_shouldThrow() {
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, "内容", List.of(), "来源");

        assertThatThrownBy(() -> chunk.updateContent("  ", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("分片内容不能为空");
    }

    @Test
    @DisplayName("markVectorSynced: 应标记为已同步")
    void markVectorSynced_shouldSetSynced() {
        KnowledgeChunk chunk = KnowledgeChunk.create(1L, "内容", List.of(), "来源");
        chunk.updateContent("新内容", List.of());

        chunk.markVectorSynced();

        assertThat(chunk.getVectorStatus()).isEqualTo(ChunkVectorStatus.SYNCED);
    }
}
