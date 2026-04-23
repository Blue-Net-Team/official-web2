package com.bluenet.web.api.converter.rolepermission;

import com.bluenet.web.api.dto.permission.PermissionRoleBatchRequestDTO;
import com.bluenet.web.api.dto.permission.RolePermissionBatchRequestDTO;
import com.bluenet.web.application.command.rolepermission.RolePermissionCommands;
import org.springframework.stereotype.Component;

/**
 * 角色权限管理请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class RolePermissionManageRequestConverter {

    /**
     * 将批量分配权限请求 DTO 转换为命令
     */
    public RolePermissionCommands.AssignPermissionsToRoleCommand toAssignPermissionsCommand(String roleName,
            RolePermissionBatchRequestDTO dto) {
        return new RolePermissionCommands.AssignPermissionsToRoleCommand(roleName, dto.getPermissionIds());
    }

    /**
     * 将批量移除权限请求 DTO 转换为命令
     */
    public RolePermissionCommands.RemovePermissionsFromRoleCommand toRemovePermissionsCommand(String roleName,
            RolePermissionBatchRequestDTO dto) {
        return new RolePermissionCommands.RemovePermissionsFromRoleCommand(roleName, dto.getPermissionIds());
    }

    /**
     * 将批量添加角色请求 DTO 转换为命令
     */
    public RolePermissionCommands.AssignRolesToPermissionCommand toAssignRolesCommand(Long permissionId,
            PermissionRoleBatchRequestDTO dto) {
        return new RolePermissionCommands.AssignRolesToPermissionCommand(permissionId, dto.getRoleNames());
    }

    /**
     * 将批量移除角色请求 DTO 转换为命令
     */
    public RolePermissionCommands.RemoveRolesFromPermissionCommand toRemoveRolesCommand(Long permissionId,
            PermissionRoleBatchRequestDTO dto) {
        return new RolePermissionCommands.RemoveRolesFromPermissionCommand(permissionId, dto.getRoleNames());
    }
}
