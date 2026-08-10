package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * College 领域实体单元测试。
 */
@DisplayName("College 领域实体测试")
class CollegeTest {

    @Test
    @DisplayName("create: 应创建新学院")
    void create_shouldCreateCollege() {
        College college = College.create("  计算机科学与技术学院  ");

        assertThat(college.getId()).isNull();
        assertThat(college.getName()).isEqualTo("计算机科学与技术学院");
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(() -> College.create("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学院名称不能为空");
    }

    @Test
    @DisplayName("rename: 应重命名学院")
    void rename_shouldRenameCollege() {
        College college = College.create("旧学院");

        college.rename("  软件学院  ");

        assertThat(college.getName()).isEqualTo("软件学院");
    }

    @Test
    @DisplayName("rename: 空名称应抛异常")
    void rename_withBlankName_shouldThrow() {
        College college = College.create("学院");

        assertThatThrownBy(() -> college.rename(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学院名称不能为空");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        College college = College.reconstruct(1L, "学院");

        assertThat(college.getId()).isEqualTo(1L);
        assertThat(college.getName()).isEqualTo("学院");
    }
}
