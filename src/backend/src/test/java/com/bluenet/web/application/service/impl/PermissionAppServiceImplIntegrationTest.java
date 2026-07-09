package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.query.permission.GetPermissionsQuery;
import com.bluenet.web.application.result.permission.PermissionResult;
import com.bluenet.web.application.service.PermissionAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionAppServiceImpl 集成测试。
 * <p>
 * 验证权限应用服务的分页查询、详情查询和树形查询行为，重点关注已分配角色的聚合。
 * </p>
 */
@DisplayName("PermissionAppServiceImpl 集成测试")
class PermissionAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PermissionAppService permissionAppService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleMapper roleMapper;

    private Long memberRoleId;
    private Long directionAdminRoleId;

    @BeforeEach
    void prepare() {
        memberRoleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        directionAdminRoleId = RoleFixture.roleId(roleMapper, RoleType.DIRECTION_ADMIN);
    }

    @Test
    @DisplayName("getPermissions: 应分页返回权限并携带已分配角色")
    void getPermissions_shouldReturnPagedResultsWithAssignedRoles() {
        Permission readPermission = PermissionFixture.save(permissionRepository, "app-test:read");
        Permission writePermission = PermissionFixture.save(permissionRepository, "app-test:write");
        Permission deletePermission = PermissionFixture.save(permissionRepository, "app-test:user:delete");

        RolePermissionFixture.grant(
                rolePermissionRepository,
                memberRoleId,
                readPermission.getId(),
                writePermission.getId());
        RolePermissionFixture.grant(
                rolePermissionRepository,
                directionAdminRoleId,
                writePermission.getId());

        GetPermissionsQuery query = new GetPermissionsQuery(null, null, 0, 2);
        Page<PermissionResult> page = permissionAppService.getPermissions(query);

        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getContent().size());

        PermissionResult readResult = findById(page, readPermission.getId());
        assertNotNull(readResult);
        assertEquals(readPermission.getValue(), readResult.value());
        assertEquals(1, readResult.assignedRoles().size());
        assertTrue(readResult.assignedRoles().contains(RoleType.MEMBER.getName()));

        PermissionResult writeResult = findById(page, writePermission.getId());
        assertNotNull(writeResult);
        assertEquals(2, writeResult.assignedRoles().size());
        assertTrue(writeResult.assignedRoles().contains(RoleType.MEMBER.getName()));
        assertTrue(writeResult.assignedRoles().contains(RoleType.DIRECTION_ADMIN.getName()));

        PermissionResult deleteResult = findById(page, deletePermission.getId());
        assertNull(deleteResult);
    }

    @Test
    @DisplayName("getPermissions: 应按关键字过滤权限")
    void getPermissions_shouldFilterByKeyword() {
        Permission alphaPermission = PermissionFixture.save(
                permissionRepository,
                "Alpha Permission",
                "app-test:alpha");
        Permission betaPermission = PermissionFixture.save(
                permissionRepository,
                "Beta Permission",
                "app-test:beta");

        GetPermissionsQuery query = new GetPermissionsQuery("alpha", null, 0, 20);
        Page<PermissionResult> page = permissionAppService.getPermissions(query);

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(alphaPermission.getId(), page.getContent().get(0).id());
        assertEquals(alphaPermission.getValue(), page.getContent().get(0).value());

        PermissionResult betaResult = findById(page, betaPermission.getId());
        assertNull(betaResult);
    }

    @Test
    @DisplayName("getPermissions: 应按权限格式过滤")
    void getPermissions_shouldFilterByFormat() {
        Permission singleColonPermission = PermissionFixture.save(permissionRepository, "app-test:read");
        Permission doubleColonPermission = PermissionFixture.save(permissionRepository, "app-test:user:delete");

        GetPermissionsQuery query = new GetPermissionsQuery(null, "resource:action", 0, 20);
        Page<PermissionResult> page = permissionAppService.getPermissions(query);

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(singleColonPermission.getId(), page.getContent().get(0).id());

        PermissionResult doubleColonResult = findById(page, doubleColonPermission.getId());
        assertNull(doubleColonResult);
    }

    @Test
    @DisplayName("getPermissionDetail: 应返回权限详情及已分配角色")
    void getPermissionDetail_shouldReturnDetailWithAssignedRoles() {
        Permission permission = PermissionFixture.save(permissionRepository, "app-test:detail");
        RolePermissionFixture.grant(rolePermissionRepository, memberRoleId, permission.getId());
        RolePermissionFixture.grant(rolePermissionRepository, directionAdminRoleId, permission.getId());

        PermissionResult result = permissionAppService.getPermissionDetail(permission.getId());

        assertEquals(permission.getId(), result.id());
        assertEquals(permission.getValue(), result.value());
        assertEquals(permission.getName(), result.name());
        assertEquals(permission.getUrl(), result.url());
        assertEquals(permission.getMethod(), result.method());
        assertEquals(permission.getAccessLevel(), result.accessLevel());
        assertEquals(2, result.assignedRoles().size());
        assertTrue(result.assignedRoles().contains(RoleType.MEMBER.getName()));
        assertTrue(result.assignedRoles().contains(RoleType.DIRECTION_ADMIN.getName()));
    }

    @Test
    @DisplayName("getPermissionDetail: 权限不存在应抛出 DataNotFound")
    void getPermissionDetail_notFound_shouldThrow() {
        assertThrows(DataNotFound.class, () -> permissionAppService.getPermissionDetail(-1L));
    }

    @Test
    @DisplayName("getPermissionTree: 应返回所有权限作为树节点并包含测试权限")
    void getPermissionTree_shouldReturnAllPermissionsAsTreeNodes() {
        Permission firstPermission = PermissionFixture.save(permissionRepository, "app-test:tree:first");
        Permission secondPermission = PermissionFixture.save(permissionRepository, "app-test:tree:second");
        Permission thirdPermission = PermissionFixture.save(permissionRepository, "app-test:tree-third");

        List<PermissionResult> tree = permissionAppService.getPermissionTree();

        assertTrue(tree.size() >= 3);

        PermissionResult firstResult = findById(tree, firstPermission.getId());
        assertNotNull(firstResult);
        assertEquals(firstPermission.getValue(), firstResult.value());
        assertEquals(List.of(), firstResult.assignedRoles());

        PermissionResult secondResult = findById(tree, secondPermission.getId());
        assertNotNull(secondResult);
        assertEquals(secondPermission.getValue(), secondResult.value());
        assertEquals(List.of(), secondResult.assignedRoles());

        PermissionResult thirdResult = findById(tree, thirdPermission.getId());
        assertNotNull(thirdResult);
        assertEquals(thirdPermission.getValue(), thirdResult.value());
        assertEquals(List.of(), thirdResult.assignedRoles());
    }

    private PermissionResult findById(Page<PermissionResult> page, Long id) {
        for (PermissionResult result : page.getContent()) {
            if (result.id().equals(id)) {
                return result;
            }
        }
        return null;
    }

    private PermissionResult findById(List<PermissionResult> results, Long id) {
        for (PermissionResult result : results) {
            if (result.id().equals(id)) {
                return result;
            }
        }
        return null;
    }
}
