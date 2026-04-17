package com.bluenet.web.domain.repository;

import java.util.List;
import java.util.Map;

public interface RolePermissionRepository {
    Map<Long, List<String>> findRoleNamesByPermissionIds(List<Long> permissionIds);

    List<String> findRoleNamesByPermissionId(Long permissionId);

    List<Long> findPermissionIdsByRoleId(Long roleId);

    List<Long> findRoleIdsByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    int batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds);

    int batchRemovePermissionsFromRole(Long roleId, List<Long> permissionIds);

    int batchAssignRolesToPermission(Long permissionId, List<Long> roleIds);

    int batchRemoveRolesFromPermission(Long permissionId, List<Long> roleIds);
}
