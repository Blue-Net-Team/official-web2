package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link RolePermission} 实体单元测试。
 */
@DisplayName("RolePermission 实体单元测试")
class RolePermissionTest {

    @Test
    @DisplayName("create: 应创建角色权限关联")
    void create_shouldCreateRolePermission() {
        RolePermission rolePermission = RolePermission.create(1L, 2L);
        assertEquals(1L, rolePermission.getRoleId());
        assertEquals(2L, rolePermission.getPermissionId());
    }

    @Test
    @DisplayName("create: 角色ID为空时应抛异常")
    void create_nullRoleId_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> RolePermission.create(null, 2L));
    }

    @Test
    @DisplayName("create: 权限ID为空时应抛异常")
    void create_nullPermissionId_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> RolePermission.create(1L, null));
    }

    @Test
    @DisplayName("reconstruct: 应重建带 ID 的关联")
    void reconstruct_shouldRestoreRolePermission() {
        RolePermission rolePermission = RolePermission.reconstruct(10L, 1L, 2L);
        assertEquals(10L, rolePermission.getId());
        assertEquals(1L, rolePermission.getRoleId());
        assertEquals(2L, rolePermission.getPermissionId());
    }
}
