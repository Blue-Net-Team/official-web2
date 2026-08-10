package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * File 领域实体单元测试。
 */
@DisplayName("File 领域实体测试")
class FileTest {

    @Test
    @DisplayName("create: 应创建待上传文件")
    void create_shouldCreatePendingFile() {
        LocalDateTime before = LocalDateTime.now();
        File file = File.create("  头像.png  ", FileType.AVATAR);
        LocalDateTime after = LocalDateTime.now();

        assertThat(file.getId()).isNull();
        assertThat(file.getName()).isEqualTo("头像.png");
        assertThat(file.getType()).isEqualTo(FileType.AVATAR);
        assertThat(file.getUrl()).isNull();
        assertThat(file.getStatus()).isEqualTo(FileStatus.PENDING);
        assertThat(file.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(file.getCreatedAt()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("create: 文件名为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(() -> File.create("   ", FileType.NORMAL_IMG))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件名不能为空");
    }

    @Test
    @DisplayName("create: 文件类型为空应抛异常")
    void create_withNullType_shouldThrow() {
        assertThatThrownBy(() -> File.create("name.png", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件类型不能为空");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        File file = File.reconstruct(
                100L,
                "文档.pdf",
                FileType.KNOWLEDGE,
                "https://example.com/doc.pdf",
                FileStatus.ACTIVE,
                createdAt);

        assertThat(file.getId()).isEqualTo(100L);
        assertThat(file.getName()).isEqualTo("文档.pdf");
        assertThat(file.getType()).isEqualTo(FileType.KNOWLEDGE);
        assertThat(file.getUrl()).isEqualTo("https://example.com/doc.pdf");
        assertThat(file.getStatus()).isEqualTo(FileStatus.ACTIVE);
        assertThat(file.getCreatedAt()).isEqualTo(createdAt);
    }
}
