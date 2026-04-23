package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.RolePermissionManageResult;
import com.bluenet.web.application.command.rolepermission.RolePermissionCommands;
import com.bluenet.web.application.service.RolePermissionManageAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色权限管理应用服务实现。
 * <p>
 * 实现角色权限管理聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RolePermissionManageAppServiceImpl implements RolePermissionManageAppService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 查询角色权限。
     *
     * @param roleName
     *            角色名称
     * @return 权限值列表
     */
    @Override
    public List<String> getRolePermissions(String roleName) {
        RoleVO role = resolveRoleIncludeSuperAdmin(roleName);
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(role.getId());
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        List<Permission> permissions = permissionRepository.findAllByIds(permissionIds);
        return permissions.stream()
                .map(Permission::getValue)
                .sorted()
                .toList();
    }

    /**
     * 分配权限给角色。
     *
     * @param command
     *            分配权限命令
     * @return 角色权限管理结果
     */
    @Override
    @Transactional
    public RolePermissionManageResult assignPermissionsToRole(
            RolePermissionCommands.AssignPermissionsToRoleCommand command) {
        RoleVO role = resolveRole(command.roleName());
        List<Long> validatedIds = resolvePermissionIds(command.permissionIds());
        int count = rolePermissionRepository.batchAssignPermissionsToRole(role.getId(), validatedIds);
        List<String> currentValues = resolvePermissionValues(
                rolePermissionRepository.findPermissionIdsByRoleId(role.getId()));
        return RolePermissionManageResult.ofPermissions(count, currentValues);
    }

    /**
     * 从角色移除权限。
     *
     * @param command
     *            移除权限命令
     * @return 角色权限管理结果
     */
    @Override
    @Transactional
    public RolePermissionManageResult removePermissionsFromRole(
            RolePermissionCommands.RemovePermissionsFromRoleCommand command) {
        RoleVO role = resolveRole(command.roleName());
        List<Long> validatedIds = resolvePermissionIds(command.permissionIds());
        int count = rolePermissionRepository.batchRemovePermissionsFromRole(role.getId(), validatedIds);
        List<String> currentValues = resolvePermissionValues(
                rolePermissionRepository.findPermissionIdsByRoleId(role.getId()));
        return RolePermissionManageResult.ofPermissions(count, currentValues);
    }

    /**
     * 查询权限角色。
     *
     * @param permissionId
     *            权限ID
     * @return 角色名称列表
     */
    @Override
    public List<String> getPermissionRoles(Long permissionId) {
        return rolePermissionRepository.findRoleNamesByPermissionId(permissionId);
    }

    /**
     * 分配角色给权限。
     *
     * @param command
     *            分配角色命令
     * @return 角色权限管理结果
     */
    @Override
    @Transactional
    public RolePermissionManageResult assignRolesToPermission(
            RolePermissionCommands.AssignRolesToPermissionCommand command) {
        List<Long> roleIds = resolveRoleIds(command.roleNames());
        int count = rolePermissionRepository.batchAssignRolesToPermission(command.permissionId(), roleIds);
        List<String> currentRoles = rolePermissionRepository.findRoleNamesByPermissionId(command.permissionId());
        return RolePermissionManageResult.ofRoles(count, currentRoles);
    }

    /**
     * 从权限移除角色。
     *
     * @param command
     *            移除角色命令
     * @return 角色权限管理结果
     */
    @Override
    @Transactional
    public RolePermissionManageResult removeRolesFromPermission(
            RolePermissionCommands.RemoveRolesFromPermissionCommand command) {
        List<Long> roleIds = resolveRoleIds(command.roleNames());
        int count = rolePermissionRepository.batchRemoveRolesFromPermission(command.permissionId(), roleIds);
        List<String> currentRoles = rolePermissionRepository.findRoleNamesByPermissionId(command.permissionId());
        return RolePermissionManageResult.ofRoles(count, currentRoles);
    }

    private RoleVO resolveRole(String roleName) {
        validateNotSuperAdmin(roleName);
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new DataNotFound("角色不存在: " + roleName));
    }

    private RoleVO resolveRoleIncludeSuperAdmin(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new DataNotFound("角色不存在: " + roleName));
    }

    private void validateNotSuperAdmin(String roleName) {
        if (RoleType.SUPER_ADMIN.name().equals(roleName)) {
            throw new IllegalArgumentException("SUPER_ADMIN 角色绕过权限检查，无需分配权限");
        }
    }

    private List<Long> resolvePermissionIds(List<Long> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllByIds(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            List<Long> foundIds = permissions.stream().map(Permission::getId).toList();
            List<Long> missingIds = permissionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new DataNotFound("权限不存在: " + missingIds);
        }
        return permissionIds;
    }

    private List<Long> resolveRoleIds(List<String> roleNames) {
        return roleNames.stream()
                .map(
                        name -> roleRepository.findByName(name)
                                .orElseThrow(() -> new DataNotFound("角色不存在: " + name)))
                .map(RoleVO::getId)
                .toList();
    }

    private List<String> resolvePermissionValues(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        List<Permission> permissions = permissionRepository.findAllByIds(permissionIds);
        return permissions.stream()
                .map(Permission::getValue)
                .sorted()
                .toList();
    }
}
