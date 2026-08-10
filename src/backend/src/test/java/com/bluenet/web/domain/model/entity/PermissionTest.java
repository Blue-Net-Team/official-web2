package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Permission} 实体单元测试。
 */
@DisplayName("Permission 实体单元测试")
class PermissionTest {

    @Test
    @DisplayName("create: 应创建权限")
    void create_shouldCreatePermission() {
        Permission permission = Permission.create("用户管理", "user:manage", "/api/v1/users", "GET", "PROTECTED");
        assertEquals("用户管理", permission.getName());
        assertEquals("user:manage", permission.getValue());
        assertEquals("/api/v1/users", permission.getUrl());
        assertEquals("GET", permission.getMethod());
        assertEquals("PROTECTED", permission.getAccessLevel());
    }

    @Test
    @DisplayName("create: 权限标识符为空时应抛异常")
    void create_blankValue_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Permission.create("用户管理", "  ", "/api/v1/users", "GET", "PROTECTED"));
        assertThrows(
                IllegalArgumentException.class,
                () -> Permission.create("用户管理", null, "/api/v1/users", "GET", "PROTECTED"));
    }

    @Test
    @DisplayName("reconstruct: 应重建带 ID 的权限")
    void reconstruct_shouldRestorePermission() {
        Permission permission = Permission.reconstruct(1L, "用户管理", "user:manage", "/api/v1/users", "GET", "PROTECTED");
        assertEquals(1L, permission.getId());
        assertEquals("user:manage", permission.getValue());
    }
}
