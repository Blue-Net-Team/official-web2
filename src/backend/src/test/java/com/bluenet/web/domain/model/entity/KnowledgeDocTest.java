package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.DocParseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * KnowledgeDoc 领域实体单元测试。
 */
@DisplayName("KnowledgeDoc 领域实体测试")
class KnowledgeDocTest {

    @Test
    @DisplayName("create: 应创建待解析文档")
    void create_shouldCreatePendingDocument() {
        LocalDateTime before = LocalDateTime.now();
        KnowledgeDoc doc = KnowledgeDoc.create(1L, "文档标题");
        LocalDateTime after = LocalDateTime.now();

        assertThat(doc.getId()).isNull();
        assertThat(doc.getFileId()).isEqualTo(1L);
        assertThat(doc.getTitle()).isEqualTo("文档标题");
        assertThat(doc.getStatus()).isEqualTo(DocParseStatus.PENDING);
        assertThat(doc.getChunkCount()).isZero();
        assertThat(doc.getErrorMessage()).isEqualTo("");
        assertThat(doc.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(doc.getCreatedAt()).isBeforeOrEqualTo(after);
        assertThat(doc.getUpdatedAt()).isAfterOrEqualTo(before);
        assertThat(doc.getUpdatedAt()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("create: 文件ID为空应抛异常")
    void create_withNullFileId_shouldThrow() {
        assertThatThrownBy(() -> KnowledgeDoc.create(null, "标题"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件ID不能为空");
    }

    @Test
    @DisplayName("create: 空标题应使用默认空字符串")
    void create_withNullTitle_shouldUseEmptyString() {
        KnowledgeDoc doc = KnowledgeDoc.create(1L, null);

        assertThat(doc.getTitle()).isEqualTo("");
    }

    @Test
    @DisplayName("markForReparse: 应重置为待解析状态")
    void markForReparse_shouldResetStatus() {
        KnowledgeDoc doc = KnowledgeDoc.create(1L, "标题");
        LocalDateTime originalUpdatedAt = doc.getUpdatedAt();

        doc.markForReparse();

        assertThat(doc.getStatus()).isEqualTo(DocParseStatus.PENDING);
        assertThat(doc.getErrorMessage()).isEqualTo("");
        assertThat(doc.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    @DisplayName("updateStatus: 应更新状态、分段数和错误信息")
    void updateStatus_shouldUpdateStatusAndCounts() {
        KnowledgeDoc doc = KnowledgeDoc.create(1L, "标题");
        LocalDateTime originalUpdatedAt = doc.getUpdatedAt();

        doc.updateStatus(DocParseStatus.COMPLETED, 10, "解析完成");

        assertThat(doc.getStatus()).isEqualTo(DocParseStatus.COMPLETED);
        assertThat(doc.getChunkCount()).isEqualTo(10);
        assertThat(doc.getErrorMessage()).isEqualTo("解析完成");
        assertThat(doc.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    @DisplayName("updateStatus: null 字段不应覆盖已有值")
    void updateStatus_withNullFields_shouldNotOverwrite() {
        KnowledgeDoc doc = KnowledgeDoc.create(1L, "标题");
        doc.updateStatus(DocParseStatus.COMPLETED, 10, "完成");

        doc.updateStatus(DocParseStatus.FAILED, null, null);

        assertThat(doc.getStatus()).isEqualTo(DocParseStatus.FAILED);
        assertThat(doc.getChunkCount()).isEqualTo(10);
        assertThat(doc.getErrorMessage()).isEqualTo("完成");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
        KnowledgeDoc doc = KnowledgeDoc.reconstruct(
                100L,
                2L,
                "标题",
                DocParseStatus.COMPLETED,
                5,
                "错误",
                createdAt,
                updatedAt);

        assertThat(doc.getId()).isEqualTo(100L);
        assertThat(doc.getFileId()).isEqualTo(2L);
        assertThat(doc.getTitle()).isEqualTo("标题");
        assertThat(doc.getStatus()).isEqualTo(DocParseStatus.COMPLETED);
        assertThat(doc.getChunkCount()).isEqualTo(5);
        assertThat(doc.getErrorMessage()).isEqualTo("错误");
        assertThat(doc.getCreatedAt()).isEqualTo(createdAt);
        assertThat(doc.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
