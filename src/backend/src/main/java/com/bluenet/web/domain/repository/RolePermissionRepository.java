package com.bluenet.web.domain.repository;

import java.util.List;
import java.util.Map;

public interface RolePermissionRepository {
    /**
     * 查询拥有任一指定权限的角色名称集合。
     *
     * @param permissionIds
     *            权限主键集合。
     * @return 满足条件的角色权限关系 结果集合。
     */
    Map<Long, List<String>> findRoleNamesByPermissionIds(List<Long> permissionIds);

    /**
     * 查询拥有指定权限的角色名称集合。
     *
     * @param permissionId
     *            权限主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<String> findRoleNamesByPermissionId(Long permissionId);

    /**
     * 查询角色已绑定的权限主键集合。
     *
     * @param roleId
     *            角色主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<Long> findPermissionIdsByRoleId(Long roleId);

    /**
     * 查询拥有指定权限的角色主键集合。
     *
     * @param permissionId
     *            权限主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<Long> findRoleIdsByPermissionId(Long permissionId);

    /**
     * 判断角色与权限是否已经存在授权关系。
     *
     * @param roleId
     *            角色主键。
     * @param permissionId
     *            权限主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 批量为角色授予权限。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 数据库受影响行数。
     */
    int batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 批量移除角色已授予的权限。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 数据库受影响行数。
     */
    int batchRemovePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 批量为权限绑定可访问角色。
     *
     * @param permissionId
     *            权限主键。
     * @param roleIds
     *            角色主键集合。
     * @return 数据库受影响行数。
     */
    int batchAssignRolesToPermission(Long permissionId, List<Long> roleIds);

    /**
     * 批量移除权限与角色的绑定关系。
     *
     * @param permissionId
     *            权限主键。
     * @param roleIds
     *            角色主键集合。
     * @return 数据库受影响行数。
     */
    int batchRemoveRolesFromPermission(Long permissionId, List<Long> roleIds);
}
