package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import org.springframework.stereotype.Component;

/**
 * 权限检查器 提供便捷的权限检查方法供业务代码使用
 */
@Component
public class PermissionChecker {

    private final PermissionCache permissionCache;

    public PermissionChecker(PermissionCache permissionCache) {
        this.permissionCache = permissionCache;
    }

    /**
     * 检查角色是否拥有指定权限
     *
     * @param roleId
     *            角色ID
     * @param permissionValue
     *            权限值
     * @return true 如果有权限
     */
    public boolean hasPermission(Long roleId, String permissionValue) {
        return permissionCache.hasPermission(roleId, permissionValue);
    }

    /**
     * 检查角色是否 >= 指定角色级别
     *
     * @param currentRoleType
     *            当前角色
     * @param minRoleType
     *            最低要求角色
     * @return true 如果当前角色级别 >= 最低要求
     */
    public boolean hasRoleLevel(RoleType currentRoleType, RoleType minRoleType) {
        return RoleHierarchy.hasRoleLevel(currentRoleType, minRoleType);
    }

    /**
     * 检查是否为超级管理员
     *
     * @param roleType
     *            角色类型
     * @return true 如果是超级管理员
     */
    public boolean isSuperAdmin(RoleType roleType) {
        return RoleHierarchy.isSuperAdmin(roleType);
    }

    /**
     * 检查是否为方向管理员或以上
     *
     * @param roleType
     *            角色类型
     * @return true 如果是方向管理员或以上
     */
    public boolean isDirectionAdminOrAbove(RoleType roleType) {
        return RoleHierarchy.isDirectionAdminOrAbove(roleType);
    }

    /**
     * 检查是否为成员或以上
     *
     * @param roleType
     *            角色类型
     * @return true 如果是成员或以上
     */
    public boolean isMemberOrAbove(RoleType roleType) {
        return RoleHierarchy.isMemberOrAbove(roleType);
    }

    /**
     * 检查权限是否是孤儿权限（无角色关联）
     *
     * @param permissionValue
     *            权限值
     * @return true 如果是孤儿权限
     */
    public boolean isOrphanPermission(String permissionValue) {
        return permissionCache.isOrphan(permissionValue);
    }
}
