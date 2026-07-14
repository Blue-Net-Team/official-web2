package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * KnowledgeTag 领域实体单元测试。
 */
@DisplayName("KnowledgeTag 领域实体测试")
class KnowledgeTagTest {

    @Test
    @DisplayName("create: 应创建新标签")
    void create_shouldCreateTag() {
        KnowledgeTag tag = KnowledgeTag.create("Java", "Java 相关文档");

        assertThat(tag.getId()).isNull();
        assertThat(tag.getTagName()).isEqualTo("Java");
        assertThat(tag.getTagDescription()).isEqualTo("Java 相关文档");
        assertThat(tag.getChunksCount()).isZero();
    }

    @Test
    @DisplayName("create: 空描述应使用默认空字符串")
    void create_withNullDescription_shouldUseEmptyString() {
        KnowledgeTag tag = KnowledgeTag.create("Java", null);

        assertThat(tag.getTagDescription()).isEqualTo("");
    }

    @Test
    @DisplayName("create: 标签名为空应抛异常")
    void create_withBlankTagName_shouldThrow() {
        assertThatThrownBy(() -> KnowledgeTag.create("   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标签名不能为空");
    }

    @Test
    @DisplayName("updateDescription: 应更新描述")
    void updateDescription_shouldUpdateDescription() {
        KnowledgeTag tag = KnowledgeTag.create("Java", "旧描述");

        tag.updateDescription("新描述");

        assertThat(tag.getTagDescription()).isEqualTo("新描述");
    }

    @Test
    @DisplayName("updateDescription: null 应设为空字符串")
    void updateDescription_withNull_shouldSetEmptyString() {
        KnowledgeTag tag = KnowledgeTag.create("Java", "描述");

        tag.updateDescription(null);

        assertThat(tag.getTagDescription()).isEqualTo("");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        KnowledgeTag tag = KnowledgeTag.reconstruct(100L, "Python", "Python 相关", 5);

        assertThat(tag.getId()).isEqualTo(100L);
        assertThat(tag.getTagName()).isEqualTo("Python");
        assertThat(tag.getTagDescription()).isEqualTo("Python 相关");
        assertThat(tag.getChunksCount()).isEqualTo(5);
    }
}
