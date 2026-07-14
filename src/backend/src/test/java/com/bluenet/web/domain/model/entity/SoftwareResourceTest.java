package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SoftwareResource 领域实体单元测试。
 */
@DisplayName("SoftwareResource 领域实体测试")
class SoftwareResourceTest {

    @Test
    @DisplayName("create: 应创建启用的软件资源")
    void create_shouldCreateActiveResource() {
        SoftwareResource resource = SoftwareResource.create(
                "  IntelliJ IDEA  ",
                SoftwareResourceDirection.GENERAL,
                "IDE",
                "Java 集成开发环境",
                "  https://jetbrains.com/idea  ",
                10);

        assertThat(resource.getId()).isNull();
        assertThat(resource.getName()).isEqualTo("IntelliJ IDEA");
        assertThat(resource.getDirection()).isEqualTo(SoftwareResourceDirection.GENERAL);
        assertThat(resource.getCategory()).isEqualTo("IDE");
        assertThat(resource.getDescription()).isEqualTo("Java 集成开发环境");
        assertThat(resource.getExternalUrl()).isEqualTo("https://jetbrains.com/idea");
        assertThat(resource.getSortOrder()).isEqualTo(10);
        assertThat(resource.getStatus()).isEqualTo(SoftwareResourceStatus.ACTIVE);
    }

    @Test
    @DisplayName("create: 排序为空应默认0")
    void create_withNullSortOrder_shouldDefaultToZero() {
        SoftwareResource resource = SoftwareResource.create(
                "软件",
                SoftwareResourceDirection.COMPUTER_VISION,
                null,
                null,
                "https://example.com",
                null);

        assertThat(resource.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(
                () -> SoftwareResource.create(
                        "   ",
                        SoftwareResourceDirection.GENERAL,
                        null,
                        null,
                        "https://example.com",
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("软件名称不能为空");
    }

    @Test
    @DisplayName("create: 方向为空应抛异常")
    void create_withNullDirection_shouldThrow() {
        assertThatThrownBy(
                () -> SoftwareResource.create(
                        "软件",
                        null,
                        null,
                        null,
                        "https://example.com",
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("方向不能为空");
    }

    @Test
    @DisplayName("create: 外部链接为空应抛异常")
    void create_withBlankExternalUrl_shouldThrow() {
        assertThatThrownBy(
                () -> SoftwareResource.create(
                        "软件",
                        SoftwareResourceDirection.GENERAL,
                        null,
                        null,
                        "   ",
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("外部链接不能为空");
    }

    @Test
    @DisplayName("update: 应更新字段")
    void update_shouldUpdateFields() {
        SoftwareResource resource = SoftwareResource.create(
                "旧软件",
                SoftwareResourceDirection.EMBEDDED,
                "旧分类",
                "旧描述",
                "https://old.example.com",
                5);

        resource.update(
                "  新软件  ",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                "新分类",
                "新描述",
                "  https://new.example.com  ",
                null,
                SoftwareResourceStatus.DISABLED);

        assertThat(resource.getName()).isEqualTo("新软件");
        assertThat(resource.getDirection()).isEqualTo(SoftwareResourceDirection.STRUCTURAL_DESIGN);
        assertThat(resource.getCategory()).isEqualTo("新分类");
        assertThat(resource.getDescription()).isEqualTo("新描述");
        assertThat(resource.getExternalUrl()).isEqualTo("https://new.example.com");
        assertThat(resource.getSortOrder()).isEqualTo(5);
        assertThat(resource.getStatus()).isEqualTo(SoftwareResourceStatus.DISABLED);
    }

    @Test
    @DisplayName("update: null 排序不应覆盖原值")
    void update_withNullSortOrder_shouldPreserveOriginal() {
        SoftwareResource resource = SoftwareResource.create(
                "软件",
                SoftwareResourceDirection.GENERAL,
                null,
                null,
                "https://example.com",
                5);

        resource.update(
                "软件2",
                SoftwareResourceDirection.GENERAL,
                null,
                null,
                "https://example2.com",
                null,
                null);

        assertThat(resource.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("changeStatus: 应切换状态")
    void changeStatus_shouldToggleStatus() {
        SoftwareResource resource = SoftwareResource.create(
                "软件",
                SoftwareResourceDirection.GENERAL,
                null,
                null,
                "https://example.com",
                null);

        resource.changeStatus(SoftwareResourceStatus.DISABLED);

        assertThat(resource.getStatus()).isEqualTo(SoftwareResourceStatus.DISABLED);
    }

    @Test
    @DisplayName("changeStatus: 状态为空应抛异常")
    void changeStatus_withNullStatus_shouldThrow() {
        SoftwareResource resource = SoftwareResource.create(
                "软件",
                SoftwareResourceDirection.GENERAL,
                null,
                null,
                "https://example.com",
                null);

        assertThatThrownBy(() -> resource.changeStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("状态不能为空");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        SoftwareResource resource = SoftwareResource.reconstruct(
                100L,
                "软件",
                SoftwareResourceDirection.COMPUTER_VISION,
                "分类",
                "描述",
                "https://example.com",
                20,
                SoftwareResourceStatus.DISABLED);

        assertThat(resource.getId()).isEqualTo(100L);
        assertThat(resource.getName()).isEqualTo("软件");
        assertThat(resource.getDirection()).isEqualTo(SoftwareResourceDirection.COMPUTER_VISION);
        assertThat(resource.getCategory()).isEqualTo("分类");
        assertThat(resource.getDescription()).isEqualTo("描述");
        assertThat(resource.getExternalUrl()).isEqualTo("https://example.com");
        assertThat(resource.getSortOrder()).isEqualTo(20);
        assertThat(resource.getStatus()).isEqualTo(SoftwareResourceStatus.DISABLED);
    }
}
