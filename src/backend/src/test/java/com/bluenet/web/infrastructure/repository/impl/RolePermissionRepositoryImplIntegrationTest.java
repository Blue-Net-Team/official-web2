package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RolePermissionRepositoryImpl 集成测试。
 * <p>
 * 验证角色权限关联仓储的保存、查询、批量授权与移除、幂等性等行为。
 * </p>
 */
@DisplayName("RolePermissionRepositoryImpl 集成测试")
class RolePermissionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleMapper roleMapper;

    private Permission createPermission(String value) {
        Permission permission = PermissionFixture.create(value);
        permissionRepository.save(permission);
        return permission;
    }

    private Long createRole(String name) {
        RoleDO role = RoleDO.builder().name(name).build();
        roleMapper.insert(role);
        return role.getId();
    }

    @Test
    @DisplayName("save: 新关联应插入并回写 ID")
    void save_newAssociation_shouldInsertAndAssignId() {
        Long roleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Permission permission = createPermission("test:save");
        RolePermission association = RolePermission.create(roleId, permission.getId());

        rolePermissionRepository.save(association);

        assertNotNull(association.getId());
        assertTrue(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId()));
    }

    @Test
    @DisplayName("findPermissionIdsByRoleId: 应返回角色已绑定的权限 ID 集合")
    void findPermissionIdsByRoleId_shouldReturnBoundPermissions() {
        Long roleId = createRole("FIND_PERMISSION_ROLE");
        Permission permission1 = createPermission("test:find-perm-1");
        Permission permission2 = createPermission("test:find-perm-2");
        RolePermissionFixture.grant(rolePermissionRepository, roleId, permission1.getId(), permission2.getId());

        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);

        assertEquals(2, permissionIds.size());
        assertTrue(permissionIds.contains(permission1.getId()));
        assertTrue(permissionIds.contains(permission2.getId()));
    }

    @Test
    @DisplayName("findRoleIdsByPermissionId: 应返回拥有指定权限的角色 ID 集合")
    void findRoleIdsByPermissionId_shouldReturnBoundRoles() {
        Long roleId1 = createRole("FIND_ROLE_1");
        Long roleId2 = createRole("FIND_ROLE_2");
        Permission permission = createPermission("test:find-role");
        RolePermissionFixture.grant(rolePermissionRepository, roleId1, permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, roleId2, permission.getId());

        List<Long> roleIds = rolePermissionRepository.findRoleIdsByPermissionId(permission.getId());

        assertEquals(2, roleIds.size());
        assertTrue(roleIds.contains(roleId1));
        assertTrue(roleIds.contains(roleId2));
    }

    @Test
    @DisplayName("findRoleNamesByPermissionId: 应返回拥有指定权限的角色名称集合")
    void findRoleNamesByPermissionId_shouldReturnRoleNames() {
        Long superAdminId = RoleFixture.roleId(roleMapper, RoleType.SUPER_ADMIN);
        Long memberId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Permission permission = createPermission("test:role-names-single");
        RolePermissionFixture.grant(rolePermissionRepository, superAdminId, permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, memberId, permission.getId());

        List<String> roleNames = rolePermissionRepository.findRoleNamesByPermissionId(permission.getId());

        assertEquals(2, roleNames.size());
        assertTrue(roleNames.contains("SUPER_ADMIN"));
        assertTrue(roleNames.contains("MEMBER"));
    }

    @Test
    @DisplayName("findRoleNamesByPermissionIds: 应按权限 ID 分组返回角色名称集合")
    void findRoleNamesByPermissionIds_shouldReturnRoleNamesGroupedByPermissionId() {
        Long superAdminId = RoleFixture.roleId(roleMapper, RoleType.SUPER_ADMIN);
        Long memberId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Permission permission1 = createPermission("test:role-names-multi-1");
        Permission permission2 = createPermission("test:role-names-multi-2");
        RolePermissionFixture.grant(rolePermissionRepository, superAdminId, permission1.getId(), permission2.getId());
        RolePermissionFixture.grant(rolePermissionRepository, memberId, permission2.getId());

        Map<Long, List<String>> roleNamesMap = rolePermissionRepository
                .findRoleNamesByPermissionIds(List.of(permission1.getId(), permission2.getId()));

        assertEquals(2, roleNamesMap.size());
        List<String> namesForPermission1 = roleNamesMap.get(permission1.getId());
        assertEquals(1, namesForPermission1.size());
        assertTrue(namesForPermission1.contains("SUPER_ADMIN"));
        List<String> namesForPermission2 = roleNamesMap.get(permission2.getId());
        assertEquals(2, namesForPermission2.size());
        assertTrue(namesForPermission2.contains("SUPER_ADMIN"));
        assertTrue(namesForPermission2.contains("MEMBER"));
    }

    @Test
    @DisplayName("existsByRoleIdAndPermissionId: 关联存在时返回 true，否则返回 false")
    void existsByRoleIdAndPermissionId_shouldReturnCorrectResult() {
        Long roleId = createRole("EXISTS_ROLE");
        Permission permission = createPermission("test:exists");

        assertFalse(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId()));

        RolePermissionFixture.grant(rolePermissionRepository, roleId, permission.getId());

        assertTrue(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId()));
    }

    @Test
    @DisplayName("batchAssignPermissionsToRole: 应批量为角色授予权限")
    void batchAssignPermissionsToRole_shouldGrantMultiplePermissions() {
        Long roleId = createRole("BATCH_ASSIGN_ROLE");
        Permission permission1 = createPermission("test:batch-assign-1");
        Permission permission2 = createPermission("test:batch-assign-2");

        int affectedRows = rolePermissionRepository
                .batchAssignPermissionsToRole(roleId, List.of(permission1.getId(), permission2.getId()));

        assertEquals(2, affectedRows);
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        assertEquals(2, permissionIds.size());
        assertTrue(permissionIds.contains(permission1.getId()));
        assertTrue(permissionIds.contains(permission2.getId()));
    }

    @Test
    @DisplayName("batchRemovePermissionsFromRole: 应批量移除角色已授予的权限")
    void batchRemovePermissionsFromRole_shouldRemoveSpecifiedPermissions() {
        Long roleId = createRole("BATCH_REMOVE_ROLE");
        Permission permission1 = createPermission("test:batch-remove-1");
        Permission permission2 = createPermission("test:batch-remove-2");
        RolePermissionFixture.grant(rolePermissionRepository, roleId, permission1.getId(), permission2.getId());

        int affectedRows = rolePermissionRepository
                .batchRemovePermissionsFromRole(roleId, List.of(permission1.getId()));

        assertEquals(1, affectedRows);
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        assertEquals(1, permissionIds.size());
        assertTrue(permissionIds.contains(permission2.getId()));
    }

    @Test
    @DisplayName("batchAssignRolesToPermission: 应批量为权限绑定角色")
    void batchAssignRolesToPermission_shouldBindMultipleRoles() {
        Long roleId1 = createRole("BATCH_ASSIGN_TO_PERM_1");
        Long roleId2 = createRole("BATCH_ASSIGN_TO_PERM_2");
        Permission permission = createPermission("test:batch-roles-to-perm");

        int affectedRows = rolePermissionRepository
                .batchAssignRolesToPermission(permission.getId(), List.of(roleId1, roleId2));

        assertEquals(2, affectedRows);
        List<Long> roleIds = rolePermissionRepository.findRoleIdsByPermissionId(permission.getId());
        assertEquals(2, roleIds.size());
        assertTrue(roleIds.contains(roleId1));
        assertTrue(roleIds.contains(roleId2));
    }

    @Test
    @DisplayName("batchRemoveRolesFromPermission: 应批量移除权限与角色的绑定")
    void batchRemoveRolesFromPermission_shouldRemoveSpecifiedRoles() {
        Long roleId1 = createRole("BATCH_REMOVE_FROM_PERM_1");
        Long roleId2 = createRole("BATCH_REMOVE_FROM_PERM_2");
        Permission permission = createPermission("test:batch-remove-roles-from-perm");
        RolePermissionFixture.grant(rolePermissionRepository, roleId1, permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, roleId2, permission.getId());

        int affectedRows = rolePermissionRepository
                .batchRemoveRolesFromPermission(permission.getId(), List.of(roleId1));

        assertEquals(1, affectedRows);
        List<Long> roleIds = rolePermissionRepository.findRoleIdsByPermissionId(permission.getId());
        assertEquals(1, roleIds.size());
        assertTrue(roleIds.contains(roleId2));
    }

    @Test
    @DisplayName("deleteById: 应删除指定角色权限关联记录")
    void deleteById_shouldRemoveAssociation() {
        Long roleId = createRole("DELETE_ROLE");
        Permission permission = createPermission("test:delete");
        RolePermission association = RolePermission.create(roleId, permission.getId());
        rolePermissionRepository.save(association);
        Long associationId = association.getId();

        rolePermissionRepository.deleteById(associationId);

        assertFalse(rolePermissionRepository.existsById(associationId));
        assertFalse(rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId()));
    }

    @Test
    @DisplayName("existsById: 关联存在时返回 true，不存在时返回 false")
    void existsById_shouldReturnCorrectResult() {
        Long roleId = createRole("EXISTS_BY_ID_ROLE");
        Permission permission = createPermission("test:exists-by-id");
        RolePermission association = RolePermission.create(roleId, permission.getId());
        rolePermissionRepository.save(association);

        assertTrue(rolePermissionRepository.existsById(association.getId()));
        assertFalse(rolePermissionRepository.existsById(-1L));
    }

    @Test
    @DisplayName("batchAssignPermissionsToRole: 重复授予同一权限应保持幂等")
    void batchAssignPermissionsToRole_shouldBeIdempotent() {
        Long roleId = createRole("IDEMPOTENT_ROLE");
        Permission permission = createPermission("test:idempotent");

        int firstAttempt = rolePermissionRepository
                .batchAssignPermissionsToRole(roleId, List.of(permission.getId()));
        int secondAttempt = rolePermissionRepository
                .batchAssignPermissionsToRole(roleId, List.of(permission.getId()));

        assertEquals(1, firstAttempt);
        assertEquals(0, secondAttempt);
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        assertEquals(1, permissionIds.size());
        assertTrue(permissionIds.contains(permission.getId()));
    }
}
