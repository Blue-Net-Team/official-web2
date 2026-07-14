package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BugReportImage 领域实体单元测试。
 */
@DisplayName("BugReportImage 领域实体测试")
class BugReportImageTest {

    @Test
    @DisplayName("create: 应创建新的图片关联")
    void create_shouldCreateImage() {
        BugReportImage image = BugReportImage.create(1L, 100L);

        assertThat(image.getId()).isNull();
        assertThat(image.getBugReportId()).isEqualTo(1L);
        assertThat(image.getFileId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("create: 应允许空值参数")
    void create_withNullValues_shouldAllowNull() {
        BugReportImage image = BugReportImage.create(null, null);

        assertThat(image.getId()).isNull();
        assertThat(image.getBugReportId()).isNull();
        assertThat(image.getFileId()).isNull();
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        BugReportImage image = BugReportImage.reconstruct(10L, 2L, 200L);

        assertThat(image.getId()).isEqualTo(10L);
        assertThat(image.getBugReportId()).isEqualTo(2L);
        assertThat(image.getFileId()).isEqualTo(200L);
    }
}
