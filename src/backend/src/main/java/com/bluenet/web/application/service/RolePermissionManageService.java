package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.permission.RolePermissionResponseDTO;
import com.bluenet.web.api.dto.permission.PermissionRoleResponseDTO;

import java.util.List;

public interface RolePermissionManageService {
    List<String> getRolePermissions(String roleName);

    RolePermissionResponseDTO assignPermissionsToRole(String roleName, List<Long> permissionIds);

    RolePermissionResponseDTO removePermissionsFromRole(String roleName, List<Long> permissionIds);

    List<String> getPermissionRoles(Long permissionId);

    PermissionRoleResponseDTO assignRolesToPermission(Long permissionId, List<String> roleNames);

    PermissionRoleResponseDTO removeRolesFromPermission(Long permissionId, List<String> roleNames);
}
