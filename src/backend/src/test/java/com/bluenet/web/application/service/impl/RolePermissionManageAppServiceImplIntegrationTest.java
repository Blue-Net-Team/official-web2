package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.rolepermission.RolePermissionCommands;
import com.bluenet.web.application.result.rolepermission.RolePermissionManageResult;
import com.bluenet.web.application.service.RolePermissionManageAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RolePermissionManageAppServiceImpl 集成测试。
 * <p>
 * 验证角色权限管理应用服务的查询、分配、移除及异常行为。
 * </p>
 */
@DisplayName("RolePermissionManageAppServiceImpl 集成测试")
class RolePermissionManageAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RolePermissionManageAppService rolePermissionManageAppService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @MockitoBean
    private PermissionCache permissionCache;

    private RoleDO createRole(String name) {
        RoleDO role = RoleDO.builder().name(name).build();
        roleMapper.insert(role);
        return role;
    }

    @Test
    @DisplayName("getRolePermissions: 应返回角色已绑定的权限值并排序")
    void getRolePermissions_shouldReturnSortedPermissionValues() {
        RoleDO role = createRole("ROLE_TEST_MEMBER");
        Permission permissionB = PermissionFixture.save(permissionRepository, "测试权限B", "test:permission:b");
        Permission permissionA = PermissionFixture.save(permissionRepository, "测试权限A", "test:permission:a");
        Permission permissionC = PermissionFixture.save(permissionRepository, "测试权限C", "test:permission:c");
        RolePermissionFixture.grant(
                rolePermissionRepository,
                role.getId(),
                permissionA.getId(),
                permissionB.getId(),
                permissionC.getId());

        List<String> result = rolePermissionManageAppService.getRolePermissions(role.getName());

        assertEquals(List.of("test:permission:a", "test:permission:b", "test:permission:c"), result);
    }

    @Test
    @DisplayName("getRolePermissions: 角色无权限时应返回空列表")
    void getRolePermissions_withoutPermissions_shouldReturnEmpty() {
        RoleDO role = createRole("ROLE_TEST_EMPTY");

        List<String> result = rolePermissionManageAppService.getRolePermissions(role.getName());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("assignPermissionsToRole: 应分配权限并返回当前权限列表")
    void assignPermissionsToRole_shouldAssignAndReturnCurrentPermissions() {
        RoleDO role = createRole("ROLE_TEST_ASSIGN");
        Permission permissionA = PermissionFixture.save(permissionRepository, "测试权限A", "test:permission:a");
        Permission permissionB = PermissionFixture.save(permissionRepository, "测试权限B", "test:permission:b");

        RolePermissionManageResult result = rolePermissionManageAppService.assignPermissionsToRole(
                new RolePermissionCommands.AssignPermissionsToRoleCommand(
                        role.getName(), List.of(permissionA.getId(), permissionB.getId())));

        assertEquals(2, result.successCount());
        assertEquals(List.of("test:permission:a", "test:permission:b"), result.currentPermissions());
    }

    @Test
    @DisplayName("assignPermissionsToRole: 不能为 SUPER_ADMIN 分配权限")
    void assignPermissionsToRole_withSuperAdmin_shouldThrow() {
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionManageAppService.assignPermissionsToRole(
                        new RolePermissionCommands.AssignPermissionsToRoleCommand(
                                RoleType.SUPER_ADMIN.getName(), List.of(permission.getId()))));
    }

    @Test
    @DisplayName("assignPermissionsToRole: 角色不存在应抛 DataNotFound")
    void assignPermissionsToRole_withRoleNotFound_shouldThrow() {
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.assignPermissionsToRole(
                        new RolePermissionCommands.AssignPermissionsToRoleCommand(
                                "ROLE_NOT_EXISTS", List.of(permission.getId()))));
    }

    @Test
    @DisplayName("assignPermissionsToRole: 权限不存在应抛 DataNotFound")
    void assignPermissionsToRole_withPermissionNotFound_shouldThrow() {
        RoleDO role = createRole("ROLE_TEST_ASSIGN_PERM_NOT_FOUND");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.assignPermissionsToRole(
                        new RolePermissionCommands.AssignPermissionsToRoleCommand(
                                role.getName(), List.of(-1L))));
    }

    @Test
    @DisplayName("removePermissionsFromRole: 应移除权限并返回当前权限列表")
    void removePermissionsFromRole_shouldRemoveAndReturnCurrentPermissions() {
        RoleDO role = createRole("ROLE_TEST_REMOVE");
        Permission permissionA = PermissionFixture.save(permissionRepository, "测试权限A", "test:permission:a");
        Permission permissionB = PermissionFixture.save(permissionRepository, "测试权限B", "test:permission:b");
        RolePermissionFixture.grant(rolePermissionRepository, role.getId(), permissionA.getId(), permissionB.getId());

        RolePermissionManageResult result = rolePermissionManageAppService.removePermissionsFromRole(
                new RolePermissionCommands.RemovePermissionsFromRoleCommand(
                        role.getName(), List.of(permissionA.getId())));

        assertEquals(1, result.successCount());
        assertEquals(List.of("test:permission:b"), result.currentPermissions());
    }

    @Test
    @DisplayName("removePermissionsFromRole: 角色不存在应抛 DataNotFound")
    void removePermissionsFromRole_withRoleNotFound_shouldThrow() {
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.removePermissionsFromRole(
                        new RolePermissionCommands.RemovePermissionsFromRoleCommand(
                                "ROLE_NOT_EXISTS", List.of(permission.getId()))));
    }

    @Test
    @DisplayName("removePermissionsFromRole: 权限不存在应抛 DataNotFound")
    void removePermissionsFromRole_withPermissionNotFound_shouldThrow() {
        RoleDO role = createRole("ROLE_TEST_REMOVE_PERM_NOT_FOUND");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.removePermissionsFromRole(
                        new RolePermissionCommands.RemovePermissionsFromRoleCommand(
                                role.getName(), List.of(-1L))));
    }

    @Test
    @DisplayName("getPermissionRoles: 应返回权限已绑定的角色名称")
    void getPermissionRoles_shouldReturnRoleNames() {
        RoleDO roleA = createRole("ROLE_TEST_PERM_A");
        RoleDO roleB = createRole("ROLE_TEST_PERM_B");
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");
        RolePermissionFixture.grant(rolePermissionRepository, roleA.getId(), permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, roleB.getId(), permission.getId());

        List<String> result = rolePermissionManageAppService.getPermissionRoles(permission.getId());

        assertEquals(2, result.size());
        assertTrue(result.contains(roleA.getName()));
        assertTrue(result.contains(roleB.getName()));
    }

    @Test
    @DisplayName("assignRolesToPermission: 应分配角色到权限并返回当前角色列表")
    void assignRolesToPermission_shouldAssignAndReturnCurrentRoles() {
        RoleDO roleA = createRole("ROLE_TEST_ASSIGN_TO_PERM_A");
        RoleDO roleB = createRole("ROLE_TEST_ASSIGN_TO_PERM_B");
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        RolePermissionManageResult result = rolePermissionManageAppService.assignRolesToPermission(
                new RolePermissionCommands.AssignRolesToPermissionCommand(
                        permission.getId(), List.of(roleA.getName(), roleB.getName())));

        assertEquals(2, result.successCount());
        assertEquals(2, result.currentRoles().size());
        assertTrue(result.currentRoles().contains(roleA.getName()));
        assertTrue(result.currentRoles().contains(roleB.getName()));
    }

    @Test
    @DisplayName("assignRolesToPermission: 角色不存在应抛 DataNotFound")
    void assignRolesToPermission_withRoleNotFound_shouldThrow() {
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.assignRolesToPermission(
                        new RolePermissionCommands.AssignRolesToPermissionCommand(
                                permission.getId(), List.of("ROLE_NOT_EXISTS"))));
    }

    @Test
    @DisplayName("removeRolesFromPermission: 应从权限移除角色并返回当前角色列表")
    void removeRolesFromPermission_shouldRemoveAndReturnCurrentRoles() {
        RoleDO roleA = createRole("ROLE_TEST_REMOVE_FROM_PERM_A");
        RoleDO roleB = createRole("ROLE_TEST_REMOVE_FROM_PERM_B");
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");
        RolePermissionFixture.grant(rolePermissionRepository, roleA.getId(), permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, roleB.getId(), permission.getId());

        RolePermissionManageResult result = rolePermissionManageAppService.removeRolesFromPermission(
                new RolePermissionCommands.RemoveRolesFromPermissionCommand(
                        permission.getId(), List.of(roleA.getName())));

        assertEquals(1, result.successCount());
        assertEquals(List.of(roleB.getName()), result.currentRoles());
    }

    @Test
    @DisplayName("removeRolesFromPermission: 角色不存在应抛 DataNotFound")
    void removeRolesFromPermission_withRoleNotFound_shouldThrow() {
        Permission permission = PermissionFixture.save(permissionRepository, "测试权限", "test:permission");

        assertThrows(
                DataNotFound.class,
                () -> rolePermissionManageAppService.removeRolesFromPermission(
                        new RolePermissionCommands.RemoveRolesFromPermissionCommand(
                                permission.getId(), List.of("ROLE_NOT_EXISTS"))));
    }
}
