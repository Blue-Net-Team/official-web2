package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Venue 领域实体单元测试。
 */
@DisplayName("Venue 领域实体测试")
class VenueTest {

    @Test
    @DisplayName("create: 应创建新场地并默认排序为0")
    void create_shouldCreateVenueWithDefaultSortOrder() {
        Venue venue = Venue.create(
                "  实验室  ",
                "主实验室",
                "团队主要办公场所",
                1L,
                null);

        assertThat(venue.getId()).isNull();
        assertThat(venue.getName()).isEqualTo("实验室");
        assertThat(venue.getSubtitle()).isEqualTo("主实验室");
        assertThat(venue.getDescription()).isEqualTo("团队主要办公场所");
        assertThat(venue.getImageFileId()).isEqualTo(1L);
        assertThat(venue.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(() -> Venue.create("   ", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("场地名称不能为空");
    }

    @Test
    @DisplayName("update: 应更新所有字段")
    void update_shouldUpdateAllFields() {
        Venue venue = Venue.create("旧场地", "旧副标题", "旧描述", 1L, 5);

        venue.update("  新场地  ", "新副标题", "新描述", 2L, 10);

        assertThat(venue.getName()).isEqualTo("新场地");
        assertThat(venue.getSubtitle()).isEqualTo("新副标题");
        assertThat(venue.getDescription()).isEqualTo("新描述");
        assertThat(venue.getImageFileId()).isEqualTo(2L);
        assertThat(venue.getSortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("update: 名称为空应抛异常")
    void update_withBlankName_shouldThrow() {
        Venue venue = Venue.create("场地", null, null, null, null);

        assertThatThrownBy(() -> venue.update("   ", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("场地名称不能为空");
    }

    @Test
    @DisplayName("updateImage: 应更新图片文件ID")
    void updateImage_shouldUpdateImageFileId() {
        Venue venue = Venue.create("场地", null, null, 1L, null);

        venue.updateImage(99L);

        assertThat(venue.getImageFileId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Venue venue = Venue.reconstruct(
                100L,
                "场地",
                "副标题",
                "描述",
                10L,
                20);

        assertThat(venue.getId()).isEqualTo(100L);
        assertThat(venue.getName()).isEqualTo("场地");
        assertThat(venue.getSubtitle()).isEqualTo("副标题");
        assertThat(venue.getDescription()).isEqualTo("描述");
        assertThat(venue.getImageFileId()).isEqualTo(10L);
        assertThat(venue.getSortOrder()).isEqualTo(20);
    }
}
