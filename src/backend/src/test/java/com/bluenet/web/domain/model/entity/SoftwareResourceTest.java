package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoftwareResourceTest {

    @Test
    void create_shouldInitializeFields() {
        SoftwareResource resource = SoftwareResource.create(
                "VS Code",
                Direction.GENERAL,
                "IDE",
                "轻量级编辑器",
                "https://code.visualstudio.com/",
                10);

        assertEquals("VS Code", resource.getName());
        assertEquals(Direction.GENERAL, resource.getDirection());
        assertEquals("IDE", resource.getCategory());
        assertEquals("https://code.visualstudio.com/", resource.getExternalUrl());
        assertEquals(10, resource.getSortOrder());
        assertEquals(SoftwareResourceStatus.ACTIVE, resource.getStatus());
    }

    @Test
    void create_withNullSortOrder_shouldDefaultToZero() {
        SoftwareResource resource = SoftwareResource.create(
                "Clion",
                Direction.COMPUTER_VISION,
                "IDE",
                "C++ IDE",
                "https://www.jetbrains.com/clion/",
                null);

        assertEquals(0, resource.getSortOrder());
    }

    @Test
    void create_withBlankName_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoftwareResource.create(
                        "  ",
                        Direction.GENERAL,
                        "IDE",
                        "desc",
                        "https://example.com",
                        1));
        assertEquals("软件名称不能为空", exception.getMessage());
    }

    @Test
    void create_withNullDirection_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoftwareResource.create(
                        "Tool",
                        null,
                        "tool",
                        "desc",
                        "https://example.com",
                        1));
        assertEquals("方向不能为空", exception.getMessage());
    }

    @Test
    void create_withBlankExternalUrl_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SoftwareResource.create(
                        "Tool",
                        Direction.GENERAL,
                        "tool",
                        "desc",
                        "   ",
                        1));
        assertEquals("外部链接不能为空", exception.getMessage());
    }

    @Test
    void update_shouldChangeFields() {
        SoftwareResource resource = SoftwareResource.create(
                "Old",
                Direction.GENERAL,
                "tool",
                "desc",
                "https://old.example.com",
                1);

        resource.update(
                "New",
                Direction.EMBEDDED,
                "IDE",
                "new desc",
                "https://new.example.com",
                2,
                SoftwareResourceStatus.DISABLED);

        assertEquals("New", resource.getName());
        assertEquals(Direction.EMBEDDED, resource.getDirection());
        assertEquals("IDE", resource.getCategory());
        assertEquals("new desc", resource.getDescription());
        assertEquals("https://new.example.com", resource.getExternalUrl());
        assertEquals(2, resource.getSortOrder());
        assertEquals(SoftwareResourceStatus.DISABLED, resource.getStatus());
    }

    @Test
    void update_withNullStatus_shouldKeepCurrentStatus() {
        SoftwareResource resource = SoftwareResource.create(
                "Tool",
                Direction.GENERAL,
                "tool",
                "desc",
                "https://example.com",
                1);

        resource.update(
                "Tool",
                Direction.GENERAL,
                "tool",
                "desc",
                "https://example.com",
                1,
                null);

        assertEquals(SoftwareResourceStatus.ACTIVE, resource.getStatus());
    }

    @Test
    void changeStatus_shouldToggleStatus() {
        SoftwareResource resource = SoftwareResource.create(
                "Tool",
                Direction.GENERAL,
                "tool",
                "desc",
                "https://example.com",
                1);

        resource.changeStatus(SoftwareResourceStatus.DISABLED);

        assertEquals(SoftwareResourceStatus.DISABLED, resource.getStatus());
    }
}
