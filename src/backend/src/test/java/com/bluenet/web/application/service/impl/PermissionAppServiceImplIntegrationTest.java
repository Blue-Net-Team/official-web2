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
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PermissionAppServiceImpl 集成测试。
 *
 * <p>
 * 验证权限应用服务的分页查询、详情查询及权限树查询逻辑。
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

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissions: 默认查询应返回包含已插入权限的分页")
    void getPermissions_defaultQuery_shouldReturnPageWithInsertedPermission() {
        Permission permission = PermissionFixture.save(
                permissionRepository,
                "测试学院权限",
                "test:college:read");

        Page<PermissionResult> result = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, null, null, null));

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(result.getContent())
                .extracting(PermissionResult::value)
                .contains(permission.getValue());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissions: 应通过 keyword 按 value 或 name 过滤权限")
    void getPermissions_withKeyword_shouldFilterByValueOrName() {
        Permission permissionByValue = PermissionFixture.save(
                permissionRepository,
                "按值匹配权限",
                "test:college:value-match");
        Permission permissionByName = PermissionFixture.save(
                permissionRepository,
                "按名称匹配权限",
                "test:college:name-match");
        PermissionFixture.save(permissionRepository, "其他权限", "test:other:read");

        Page<PermissionResult> resultByValue = permissionAppService.getPermissions(
                new GetPermissionsQuery("value-match", null, null, null));
        assertThat(resultByValue.getContent())
                .extracting(PermissionResult::value)
                .containsExactly(permissionByValue.getValue());

        Page<PermissionResult> resultByName = permissionAppService.getPermissions(
                new GetPermissionsQuery("按名称匹配", null, null, null));
        assertThat(resultByName.getContent())
                .extracting(PermissionResult::value)
                .containsExactly(permissionByName.getValue());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissions: 应通过 format 按权限标识格式过滤")
    void getPermissions_withFormat_shouldFilterByAccessFormat() {
        Permission twoColonPermission = PermissionFixture.save(
                permissionRepository,
                "两层资源权限",
                "test:sub:read");
        Permission dashPermission = PermissionFixture.save(
                permissionRepository,
                "横杠格式权限",
                "test-action-read");
        Permission oneColonPermission = PermissionFixture.save(
                permissionRepository,
                "单层资源权限",
                "test:read");

        Page<PermissionResult> resourceActionResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, "resource:action", null, null));
        assertThat(resourceActionResult.getContent())
                .extracting(PermissionResult::value)
                .containsExactly(oneColonPermission.getValue());

        Page<PermissionResult> resourceSubresourceActionResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, "resource:subresource:action", null, null));
        assertThat(resourceSubresourceActionResult.getContent())
                .extracting(PermissionResult::value)
                .containsExactly(twoColonPermission.getValue());

        Page<PermissionResult> resourceActionActionResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, "resource-action:action", null, null));
        assertThat(resourceActionActionResult.getContent())
                .extracting(PermissionResult::value)
                .containsExactly(dashPermission.getValue());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissions: 自定义分页参数生效且 pageSize 被限制在 [1,100]")
    void getPermissions_withCustomPageAndSize_shouldClampPageSize() {
        for (int i = 0; i < 5; i++) {
            PermissionFixture.save(permissionRepository, "分页权限" + i, "test:page:" + i);
        }

        Page<PermissionResult> pageResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, null, 0, 2));
        assertThat(pageResult.getNumber()).isZero();
        assertThat(pageResult.getSize()).isEqualTo(2);
        assertThat(pageResult.getContent()).hasSize(2);

        Page<PermissionResult> oversizedResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, null, 0, 200));
        assertThat(oversizedResult.getSize()).isEqualTo(100);

        Page<PermissionResult> undersizedResult = permissionAppService.getPermissions(
                new GetPermissionsQuery(null, null, 0, 0));
        assertThat(undersizedResult.getSize()).isEqualTo(1);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissions: 应返回权限已分配的角色名称")
    void getPermissions_shouldReturnAssignedRoleNames() {
        Long memberRoleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Long directionAdminRoleId = RoleFixture.roleId(roleMapper, RoleType.DIRECTION_ADMIN);
        Permission permission = PermissionFixture.save(
                permissionRepository,
                "带角色权限",
                "test:college:with-roles");
        RolePermissionFixture.grant(
                rolePermissionRepository,
                memberRoleId,
                permission.getId());
        RolePermissionFixture.grant(
                rolePermissionRepository,
                directionAdminRoleId,
                permission.getId());

        Page<PermissionResult> result = permissionAppService.getPermissions(
                new GetPermissionsQuery("test:college:with-roles", null, null, null));

        assertThat(result.getContent()).hasSize(1);
        PermissionResult permissionResult = result.getContent().get(0);
        assertThat(permissionResult.assignedRoles())
                .containsExactlyInAnyOrder(
                        RoleType.MEMBER.getName(),
                        RoleType.DIRECTION_ADMIN.getName());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissionDetail: 应返回权限详情及其已分配角色名称")
    void getPermissionDetail_shouldReturnPermissionAndAssignedRoleNames() {
        Long superAdminRoleId = RoleFixture.roleId(roleMapper, RoleType.SUPER_ADMIN);
        Permission permission = PermissionFixture.save(
                permissionRepository,
                "详情权限",
                "test:college:detail");
        RolePermissionFixture.grant(
                rolePermissionRepository,
                superAdminRoleId,
                permission.getId());

        PermissionResult result = permissionAppService.getPermissionDetail(permission.getId());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(permission.getId());
        assertThat(result.value()).isEqualTo(permission.getValue());
        assertThat(result.name()).isEqualTo(permission.getName());
        assertThat(result.assignedRoles()).containsExactly(RoleType.SUPER_ADMIN.getName());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissionDetail: 不存在的 id 应抛 DataNotFound")
    void getPermissionDetail_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> permissionAppService.getPermissionDetail(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("权限不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getPermissionTree: 应返回所有权限")
    void getPermissionTree_shouldReturnAllPermissions() {
        Permission permissionA = PermissionFixture.save(
                permissionRepository,
                "树权限A",
                "test:college:tree-a");
        Permission permissionB = PermissionFixture.save(
                permissionRepository,
                "树权限B",
                "test:college:tree-b");

        List<PermissionResult> result = permissionAppService.getPermissionTree();

        List<String> values = result.stream()
                .map(PermissionResult::value)
                .collect(Collectors.toList());
        assertThat(values)
                .contains(permissionA.getValue(), permissionB.getValue());
    }
}
