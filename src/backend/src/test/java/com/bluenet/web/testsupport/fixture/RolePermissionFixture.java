package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.repository.RolePermissionRepository;

import java.util.Arrays;

/**
 * 角色权限关联测试夹具。
 */
public final class RolePermissionFixture {

    private RolePermissionFixture() {
    }

    public static RolePermission create(Long roleId, Long permissionId) {
        return RolePermission.create(roleId, permissionId);
    }

    public static void grant(RolePermissionRepository rolePermissionRepository,
            Long roleId, Long... permissionIds) {
        for (Long permissionId : permissionIds) {
            rolePermissionRepository.save(RolePermission.create(roleId, permissionId));
        }
    }

    public static void grantAll(RolePermissionRepository rolePermissionRepository,
            Long roleId, Iterable<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            rolePermissionRepository.save(RolePermission.create(roleId, permissionId));
        }
    }

    public static void revoke(RolePermissionRepository rolePermissionRepository,
            Long roleId, Long... permissionIds) {
        rolePermissionRepository.batchRemovePermissionsFromRole(roleId, Arrays.asList(permissionIds));
    }
}
