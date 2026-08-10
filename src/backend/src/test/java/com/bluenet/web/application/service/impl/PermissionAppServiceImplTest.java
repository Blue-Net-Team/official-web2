package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.application.query.permission.GetPermissionsQuery;
import com.bluenet.web.application.result.permission.PermissionResult;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * PermissionAppServiceImpl 单元测试。
 *
 * <p>
 * Application 层仅编排查询逻辑，无事务与多 Repository 写操作，因此 mock 下层 Repository，
 * 验证应用服务层的编排、参数传递、异常抛出与结果转换。
 * </p>
 */
@DisplayName("PermissionAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class PermissionAppServiceImplTest {

    @InjectMocks
    private PermissionAppServiceImpl permissionAppService;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    private Permission createPermission(Long id, String value) {
        return Permission.reconstruct(
                id,
                "权限" + value,
                value,
                "/api/v1/test/" + value.replace(':', '-'),
                "GET",
                "PROTECTED");
    }

    @Test
    @DisplayName("getPermissions: 应分页返回权限并携带已分配角色")
    void getPermissions_shouldReturnPagedResultsWithAssignedRoles() {
        Permission readPermission = createPermission(1L, "app-test:read");
        Permission writePermission = createPermission(2L, "app-test:write");
        Permission deletePermission = createPermission(3L, "app-test:user:delete");

        Pageable pageable = PageRequest.of(0, 2);
        Page<Permission> permissionPage = new PageImpl<>(
                List.of(readPermission, writePermission),
                pageable,
                3);

        when(permissionRepository.findAll(eq((String) null), eq((String) null), any(Pageable.class)))
                .thenReturn(permissionPage);
        when(rolePermissionRepository.findRoleNamesByPermissionIds(List.of(1L, 2L)))
                .thenReturn(
                        Map.of(
                                readPermission.getId(),
                                List.of(RoleType.MEMBER.getName()),
                                writePermission.getId(),
                                List.of(
                                        RoleType.MEMBER.getName(),
                                        RoleType.DIRECTION_ADMIN.getName())));

        GetPermissionsQuery query = new GetPermissionsQuery(null, null, 0, 2);
        PageDTO<PermissionResult> page = PageDTO.from(permissionAppService.getPermissions(query));

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
        Permission alphaPermission = createPermission(10L, "app-test:alpha");
        Permission betaPermission = createPermission(11L, "app-test:beta");

        Pageable pageable = PageRequest.of(0, 20);
        Page<Permission> permissionPage = new PageImpl<>(
                List.of(alphaPermission),
                pageable,
                1);

        when(permissionRepository.findAll(eq("alpha"), eq((String) null), any(Pageable.class)))
                .thenReturn(permissionPage);
        when(rolePermissionRepository.findRoleNamesByPermissionIds(List.of(alphaPermission.getId())))
                .thenReturn(Map.of(alphaPermission.getId(), List.of()));

        GetPermissionsQuery query = new GetPermissionsQuery("alpha", null, 0, 20);
        PageDTO<PermissionResult> page = PageDTO.from(permissionAppService.getPermissions(query));

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
        Permission singleColonPermission = createPermission(20L, "app-test:read");
        Permission doubleColonPermission = createPermission(21L, "app-test:user:delete");

        Pageable pageable = PageRequest.of(0, 20);
        Page<Permission> permissionPage = new PageImpl<>(
                List.of(singleColonPermission),
                pageable,
                1);

        when(permissionRepository.findAll(eq((String) null), eq("resource:action"), any(Pageable.class)))
                .thenReturn(permissionPage);
        when(rolePermissionRepository.findRoleNamesByPermissionIds(List.of(singleColonPermission.getId())))
                .thenReturn(Map.of(singleColonPermission.getId(), List.of()));

        GetPermissionsQuery query = new GetPermissionsQuery(null, "resource:action", 0, 20);
        PageDTO<PermissionResult> page = PageDTO.from(permissionAppService.getPermissions(query));

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(singleColonPermission.getId(), page.getContent().get(0).id());

        PermissionResult doubleColonResult = findById(page, doubleColonPermission.getId());
        assertNull(doubleColonResult);
    }

    @Test
    @DisplayName("getPermissionDetail: 应返回权限详情及已分配角色")
    void getPermissionDetail_shouldReturnDetailWithAssignedRoles() {
        Permission permission = createPermission(30L, "app-test:detail");

        when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.findRoleNamesByPermissionId(permission.getId()))
                .thenReturn(List.of(RoleType.MEMBER.getName(), RoleType.DIRECTION_ADMIN.getName()));

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
        when(permissionRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> permissionAppService.getPermissionDetail(-1L));
    }

    @Test
    @DisplayName("getPermissionTree: 应返回所有权限作为树节点并包含测试权限")
    void getPermissionTree_shouldReturnAllPermissionTreeNodes() {
        Permission firstPermission = createPermission(40L, "app-test:tree:first");
        Permission secondPermission = createPermission(41L, "app-test:tree:second");
        Permission thirdPermission = createPermission(42L, "app-test:tree-third");

        when(permissionRepository.findAll()).thenReturn(
                List.of(firstPermission, secondPermission, thirdPermission));

        List<PermissionResult> tree = permissionAppService.getPermissionTree();

        assertEquals(3, tree.size());

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

    private PermissionResult findById(PageDTO<PermissionResult> page, Long id) {
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
