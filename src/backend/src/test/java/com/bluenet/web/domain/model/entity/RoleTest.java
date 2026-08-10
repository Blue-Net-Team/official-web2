package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Role} 实体单元测试。
 */
@DisplayName("Role 实体单元测试")
class RoleTest {

    @Test
    @DisplayName("create: 应创建角色并去除首尾空格")
    void create_shouldTrimName() {
        Role role = Role.create("  ADMIN  ");
        assertEquals("ADMIN", role.getName());
    }

    @Test
    @DisplayName("create: 名称为空时应抛异常")
    void create_blankName_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> Role.create("  "));
        assertThrows(IllegalArgumentException.class, () -> Role.create(null));
    }

    @Test
    @DisplayName("reconstruct: 应重建带 ID 的角色")
    void reconstruct_shouldRestoreRole() {
        Role role = Role.reconstruct(1L, "ADMIN");
        assertEquals(1L, role.getId());
        assertEquals("ADMIN", role.getName());
    }
}
