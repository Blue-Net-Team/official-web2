package com.bluenet.web.domain.model.policy;

import com.bluenet.web.domain.model.enumerate.RoleType;

/**
 * 角色层级规则。
 *
 * <p>
 * 基于领域角色级别判断角色继承关系，不依赖 Web 或安全框架。
 * </p>
 */
public class RoleHierarchy {

    private RoleHierarchy() {
    }

    public static boolean hasRoleLevel(RoleType currentRole, RoleType requiredRole) {
        if (currentRole == null || requiredRole == null) {
            return false;
        }
        return currentRole.isAtLeast(requiredRole);
    }

    public static boolean isSuperAdmin(RoleType role) {
        return role == RoleType.SUPER_ADMIN;
    }

    public static boolean isDirectionAdminOrAbove(RoleType role) {
        return role != null && role.isAtLeast(RoleType.DIRECTION_ADMIN);
    }

    public static boolean isMemberOrAbove(RoleType role) {
        return role != null && role.isAtLeast(RoleType.MEMBER);
    }

    public static boolean hasRoleLevel(String currentRoleName, String requiredRoleName) {
        RoleType current = RoleType.fromName(currentRoleName);
        RoleType required = RoleType.fromName(requiredRoleName);
        return hasRoleLevel(current, required);
    }
}
