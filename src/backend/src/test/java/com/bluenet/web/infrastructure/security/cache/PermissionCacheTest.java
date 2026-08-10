package com.bluenet.web.infrastructure.security.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.infrastructure.repository.converter.PermissionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;

/**
 * PermissionCache 单元测试。
 * <p>
 * 验证启动时缓存加载、按值查询、按角色查询、孤儿权限判断等核心行为。
 * </p>
 */
@DisplayName("PermissionCache 单元测试")
@ExtendWith(MockitoExtension.class)
class PermissionCacheTest {

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private PermissionRepositoryConverter permissionRepositoryConverter;

    private PermissionCache permissionCache;

    @BeforeEach
    void setUp() {
        permissionCache = new PermissionCache(permissionMapper, rolePermissionMapper, permissionRepositoryConverter);
    }

    @Test
    @DisplayName("refresh 应加载权限与角色权限关系到缓存")
    void refresh_shouldLoadPermissionsAndRolePermissions() {
        PermissionDO permissionDo = PermissionDO.builder()
                .id(1L)
                .name("用户查看")
                .value("user:view")
                .url("/api/v1/users")
                .method("GET")
                .accessLevel("PROTECTED")
                .build();
        Permission permission = Permission.reconstruct(1L, "用户查看", "user:view", "/api/v1/users", "GET", "PROTECTED");
        RolePermissionDO rolePermission = RolePermissionDO.builder()
                .id(10L)
                .roleId(2L)
                .permissionId(1L)
                .build();

        when(permissionMapper.selectList(null)).thenReturn(List.of(permissionDo));
        when(permissionRepositoryConverter.toEntity(permissionDo)).thenReturn(permission);
        when(rolePermissionMapper.selectList(null)).thenReturn(List.of(rolePermission));

        permissionCache.refresh();

        Collection<Permission> allPermissions = permissionCache.getAllPermissions();
        assertEquals(1, allPermissions.size());
        assertTrue(allPermissions.contains(permission));

        Set<String> rolePermissions = permissionCache.getPermissionsByRole(2L);
        assertEquals(Set.of("user:view"), rolePermissions);

        assertTrue(permissionCache.hasPermission(2L, "user:view"));
        assertFalse(permissionCache.hasPermission(2L, "user:create"));
    }

    @Test
    @DisplayName("getByValue 应根据权限值返回对应权限")
    void getByValue_shouldReturnPermissionByValue() {
        Permission permission = Permission.reconstruct(1L, "用户创建", "user:create", "/api/v1/users", "POST", "PROTECTED");
        PermissionDO permissionDo = PermissionDO.builder()
                .id(1L)
                .name("用户创建")
                .value("user:create")
                .url("/api/v1/users")
                .method("POST")
                .accessLevel("PROTECTED")
                .build();

        when(permissionMapper.selectList(null)).thenReturn(List.of(permissionDo));
        when(permissionRepositoryConverter.toEntity(permissionDo)).thenReturn(permission);
        when(rolePermissionMapper.selectList(null)).thenReturn(Collections.emptyList());

        permissionCache.refresh();

        Permission result = permissionCache.getByValue("user:create");
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user:create", result.getValue());

        assertEquals(permission, permissionCache.getByValue("user:create"));
    }

    @Test
    @DisplayName("exists 应判断权限值是否在缓存中")
    void exists_shouldCheckPermissionValuePresence() {
        Permission permission = Permission
                .reconstruct(1L, "用户删除", "user:delete", "/api/v1/users/{id}", "DELETE", "PROTECTED");
        PermissionDO permissionDo = PermissionDO.builder()
                .id(1L)
                .name("用户删除")
                .value("user:delete")
                .url("/api/v1/users/{id}")
                .method("DELETE")
                .accessLevel("PROTECTED")
                .build();

        when(permissionMapper.selectList(null)).thenReturn(List.of(permissionDo));
        when(permissionRepositoryConverter.toEntity(permissionDo)).thenReturn(permission);
        when(rolePermissionMapper.selectList(null)).thenReturn(Collections.emptyList());

        permissionCache.refresh();

        assertTrue(permissionCache.exists("user:delete"));
        assertFalse(permissionCache.exists("user:update"));
    }

    @Test
    @DisplayName("getAllPermissionValues 应返回所有权限值的副本")
    void getAllPermissionValues_shouldReturnCopyOfAllValues() {
        Permission permission = Permission.reconstruct(1L, "用户列表", "user:list", "/api/v1/users", "GET", "PROTECTED");
        PermissionDO permissionDo = PermissionDO.builder()
                .id(1L)
                .name("用户列表")
                .value("user:list")
                .url("/api/v1/users")
                .method("GET")
                .accessLevel("PROTECTED")
                .build();

        when(permissionMapper.selectList(null)).thenReturn(List.of(permissionDo));
        when(permissionRepositoryConverter.toEntity(permissionDo)).thenReturn(permission);
        when(rolePermissionMapper.selectList(null)).thenReturn(Collections.emptyList());

        permissionCache.refresh();

        Set<String> values = permissionCache.getAllPermissionValues();
        assertEquals(Set.of("user:list"), values);

        values.add("should:not-affect-cache");
        assertEquals(Set.of("user:list"), permissionCache.getAllPermissionValues());
    }

    @Test
    @DisplayName("isOrphan 应正确识别未关联任何角色的权限")
    void isOrphan_shouldIdentifyPermissionsWithoutRoles() {
        Permission orphanPermission = Permission
                .reconstruct(1L, "孤立权限", "orphan:action", "/api/v1/orphan", "GET", "PROTECTED");
        Permission linkedPermission = Permission
                .reconstruct(2L, "已关联权限", "linked:action", "/api/v1/linked", "GET", "PROTECTED");
        PermissionDO orphanDo = PermissionDO.builder()
                .id(1L)
                .name("孤立权限")
                .value("orphan:action")
                .url("/api/v1/orphan")
                .method("GET")
                .accessLevel("PROTECTED")
                .build();
        PermissionDO linkedDo = PermissionDO.builder()
                .id(2L)
                .name("已关联权限")
                .value("linked:action")
                .url("/api/v1/linked")
                .method("GET")
                .accessLevel("PROTECTED")
                .build();
        RolePermissionDO rolePermission = RolePermissionDO.builder()
                .id(10L)
                .roleId(1L)
                .permissionId(2L)
                .build();

        when(permissionMapper.selectList(null)).thenReturn(List.of(orphanDo, linkedDo));
        when(permissionRepositoryConverter.toEntity(orphanDo)).thenReturn(orphanPermission);
        when(permissionRepositoryConverter.toEntity(linkedDo)).thenReturn(linkedPermission);
        when(rolePermissionMapper.selectList(null)).thenReturn(List.of(rolePermission));

        permissionCache.refresh();

        assertTrue(permissionCache.isOrphan("orphan:action"));
        assertFalse(permissionCache.isOrphan("linked:action"));
        assertTrue(permissionCache.isOrphan("non:existent"));
    }

    @Test
    @DisplayName("getPermissionsByRole 对未关联角色应返回空集合")
    void getPermissionsByRole_unmappedRole_shouldReturnEmptySet() {
        when(permissionMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(rolePermissionMapper.selectList(null)).thenReturn(Collections.emptyList());

        permissionCache.refresh();

        Set<String> result = permissionCache.getPermissionsByRole(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("init 应委托 refresh 完成缓存初始化")
    void init_shouldDelegateToRefresh() {
        when(permissionMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(rolePermissionMapper.selectList(null)).thenReturn(Collections.emptyList());

        permissionCache.init();

        verify(permissionMapper).selectList(null);
        verify(rolePermissionMapper).selectList(null);
    }
}
