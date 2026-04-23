package com.bluenet.web.application.service;

import com.bluenet.web.application.RolePermissionManageResult;
import com.bluenet.web.application.command.rolepermission.RolePermissionCommands;

import java.util.List;

/**
 * 角色权限管理应用服务接口。
 * <p>
 * 定义了角色权限管理聚合在应用层的所有业务操作。
 * </p>
 */
public interface RolePermissionManageAppService {

    /**
     * 获取角色权限列表
     *
     * @param roleName
     *            角色名称
     * @return 权限标识符列表
     */
    List<String> getRolePermissions(String roleName);

    /**
     * 批量分配权限给角色
     *
     * @param command
     *            分配命令
     * @return 操作结果
     */
    RolePermissionManageResult assignPermissionsToRole(RolePermissionCommands.AssignPermissionsToRoleCommand command);

    /**
     * 批量移除角色权限
     *
     * @param command
     *            移除命令
     * @return 操作结果
     */
    RolePermissionManageResult removePermissionsFromRole(
            RolePermissionCommands.RemovePermissionsFromRoleCommand command);

    /**
     * 获取权限对应的角色列表
     *
     * @param permissionId
     *            权限ID
     * @return 角色名称列表
     */
    List<String> getPermissionRoles(Long permissionId);

    /**
     * 批量添加角色到权限
     *
     * @param command
     *            添加命令
     * @return 操作结果
     */
    RolePermissionManageResult assignRolesToPermission(RolePermissionCommands.AssignRolesToPermissionCommand command);

    /**
     * 批量从权限移除角色
     *
     * @param command
     *            移除命令
     * @return 操作结果
     */
    RolePermissionManageResult removeRolesFromPermission(
            RolePermissionCommands.RemoveRolesFromPermissionCommand command);
}
