package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Equipment 领域实体单元测试。
 */
@DisplayName("Equipment 领域实体测试")
class EquipmentTest {

    @Test
    @DisplayName("create: 应创建设备并默认排序为0")
    void create_shouldCreateEquipmentWithDefaultSortOrder() {
        Equipment equipment = Equipment.create(
                "  3D 打印机  ",
                "Prusa",
                "用于快速原型制作",
                1L,
                null);

        assertThat(equipment.getId()).isNull();
        assertThat(equipment.getName()).isEqualTo("3D 打印机");
        assertThat(equipment.getBrand()).isEqualTo("Prusa");
        assertThat(equipment.getDescription()).isEqualTo("用于快速原型制作");
        assertThat(equipment.getImageFileId()).isEqualTo(1L);
        assertThat(equipment.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(() -> Equipment.create("   ", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("设备名称不能为空");
    }

    @Test
    @DisplayName("update: 应更新所有字段")
    void update_shouldUpdateAllFields() {
        Equipment equipment = Equipment.create("旧设备", "旧品牌", "旧描述", 1L, 5);

        equipment.update("  新设备  ", "新品牌", "新描述", 2L, 10);

        assertThat(equipment.getName()).isEqualTo("新设备");
        assertThat(equipment.getBrand()).isEqualTo("新品牌");
        assertThat(equipment.getDescription()).isEqualTo("新描述");
        assertThat(equipment.getImageFileId()).isEqualTo(2L);
        assertThat(equipment.getSortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("update: 名称为空应抛异常")
    void update_withBlankName_shouldThrow() {
        Equipment equipment = Equipment.create("设备", null, null, null, null);

        assertThatThrownBy(() -> equipment.update("   ", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("设备名称不能为空");
    }

    @Test
    @DisplayName("updateImage: 应更新图片文件ID")
    void updateImage_shouldUpdateImageFileId() {
        Equipment equipment = Equipment.create("设备", null, null, 1L, null);

        equipment.updateImage(99L);

        assertThat(equipment.getImageFileId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Equipment equipment = Equipment.reconstruct(
                100L,
                "设备",
                "品牌",
                "描述",
                10L,
                20);

        assertThat(equipment.getId()).isEqualTo(100L);
        assertThat(equipment.getName()).isEqualTo("设备");
        assertThat(equipment.getBrand()).isEqualTo("品牌");
        assertThat(equipment.getDescription()).isEqualTo("描述");
        assertThat(equipment.getImageFileId()).isEqualTo(10L);
        assertThat(equipment.getSortOrder()).isEqualTo(20);
    }
}
