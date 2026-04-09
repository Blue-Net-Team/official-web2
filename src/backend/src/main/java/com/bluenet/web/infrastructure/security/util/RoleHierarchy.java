package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.domain.model.enumerate.RoleType;

/**
 * 角色层级工具类。
 *
 * <p>
 * 处理角色之间的继承关系，用于权限判断。
 *
 * <p>
 * 角色层级关系：SUPER_ADMIN (4) &gt; DIRECTION_ADMIN (3) &gt; MEMBER (2) &gt;
 * CANDIDATE (1)
 *
 * <p>
 * <b>使用示例：</b>
 *
 * <pre>
 * {@code
 * // 检查用户是否拥有指定角色级别
 * if (RoleHierarchy.hasRoleLevel(userRole, RoleType.MEMBER)) {
 *     // 用户是成员或更高级别
 * }
 *
 * // 便捷方法
 * RoleHierarchy.isSuperAdmin(role);           // 是否为超级管理员
 * RoleHierarchy.isDirectionAdminOrAbove(role); // 是否为方向管理员或以上
 * RoleHierarchy.isMemberOrAbove(role);         // 是否为成员或以上
 *
 * // 通过角色名称检查
 * RoleHierarchy.hasRoleLevel("MEMBER", "CANDIDATE"); // true
 * }
 * </pre>
 *
 * @see RoleType
 */
public class RoleHierarchy {

    /**
     * 检查角色是否拥有目标角色的权限。
     *
     * <p>
     * 基于角色级别判断：SUPER_ADMIN &gt; DIRECTION_ADMIN &gt; MEMBER &gt; CANDIDATE
     *
     * @param currentRole
     *            当前角色
     * @param requiredRole
     *            所需角色
     * @return true 如果当前角色级别 &gt;= 所需角色级别
     */
    public static boolean hasRoleLevel(RoleType currentRole, RoleType requiredRole) {
        if (currentRole == null || requiredRole == null) {
            return false;
        }
        return currentRole.isAtLeast(requiredRole);
    }

    /**
     * 检查是否为超级管理员。
     *
     * @param role
     *            要检查的角色
     * @return true 如果是超级管理员
     */
    public static boolean isSuperAdmin(RoleType role) {
        return role == RoleType.SUPER_ADMIN;
    }

    /**
     * 检查是否为方向管理员或以上。
     *
     * @param role
     *            要检查的角色
     * @return true 如果是方向管理员或超级管理员
     */
    public static boolean isDirectionAdminOrAbove(RoleType role) {
        return role != null && role.isAtLeast(RoleType.DIRECTION_ADMIN);
    }

    /**
     * 检查是否为成员或以上。
     *
     * @param role
     *            要检查的角色
     * @return true 如果是成员、方向管理员或超级管理员
     */
    public static boolean isMemberOrAbove(RoleType role) {
        return role != null && role.isAtLeast(RoleType.MEMBER);
    }

    /**
     * 根据角色名称检查权限级别。
     *
     * <p>
     * 便捷方法，内部调用 {@link RoleType#fromName(String)} 转换角色名称。
     *
     * @param currentRoleName
     *            当前角色名称
     * @param requiredRoleName
     *            所需角色名称
     * @return true 如果当前角色级别 &gt;= 所需角色级别
     */
    public static boolean hasRoleLevel(String currentRoleName, String requiredRoleName) {
        RoleType current = RoleType.fromName(currentRoleName);
        RoleType required = RoleType.fromName(requiredRoleName);
        return hasRoleLevel(current, required);
    }
}
