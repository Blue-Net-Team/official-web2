package com.bluenet.web.application.command.rolepermission;

import java.util.List;

/**
 * 角色权限聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class RolePermissionCommands {

    /** 禁止实例化。 */
    private RolePermissionCommands() {
    }

    /**
     * 批量分配权限给角色命令。
     * <p>
     * 用于将权限批量分配给指定角色。
     * </p>
     */
    public record AssignPermissionsToRoleCommand(
            /** 角色名称 */
            String roleName,
            /** 权限ID列表 */
            List<Long> permissionIds) {
    }

    /**
     * 批量移除角色权限命令。
     * <p>
     * 用于从角色批量移除权限。
     * </p>
     */
    public record RemovePermissionsFromRoleCommand(
            /** 角色名称 */
            String roleName,
            /** 权限ID列表 */
            List<Long> permissionIds) {
    }

    /**
     * 批量添加角色到权限命令。
     * <p>
     * 用于将角色批量添加到指定权限。
     * </p>
     */
    public record AssignRolesToPermissionCommand(
            /** 权限ID */
            Long permissionId,
            /** 角色名称列表 */
            List<String> roleNames) {
    }

    /**
     * 批量从权限移除角色命令。
     * <p>
     * 用于从权限批量移除角色。
     * </p>
     */
    public record RemoveRolesFromPermissionCommand(
            /** 权限ID */
            Long permissionId,
            /** 角色名称列表 */
            List<String> roleNames) {
    }
}
