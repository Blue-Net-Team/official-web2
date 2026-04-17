package com.bluenet.web.domain.service;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.PermissionVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionDomainService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleVO resolveRole(String roleName) {
        validateNotSuperAdmin(roleName);
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new DataNotFound("角色不存在: " + roleName));
    }

    public RoleVO resolveRoleIncludeSuperAdmin(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new DataNotFound("角色不存在: " + roleName));
    }

    public void validateNotSuperAdmin(String roleName) {
        if (RoleType.SUPER_ADMIN.name().equals(roleName)) {
            throw new IllegalArgumentException("SUPER_ADMIN 角色绕过权限检查，无需分配权限");
        }
    }

    public List<Long> resolvePermissionIds(List<Long> permissionIds) {
        List<PermissionVO> permissions = permissionRepository.findAllByIds(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            List<Long> foundIds = permissions.stream().map(PermissionVO::getId).toList();
            List<Long> missingIds = permissionIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new DataNotFound("权限不存在: " + missingIds);
        }
        return permissionIds;
    }

    public List<Long> resolveRoleIds(List<String> roleNames) {
        return roleNames.stream()
                .map(
                        name -> roleRepository.findByName(name)
                                .orElseThrow(() -> new DataNotFound("角色不存在: " + name)))
                .map(RoleVO::getId)
                .toList();
    }

    @Transactional
    public int assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        return rolePermissionRepository.batchAssignPermissionsToRole(roleId, permissionIds);
    }

    @Transactional
    public int removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        return rolePermissionRepository.batchRemovePermissionsFromRole(roleId, permissionIds);
    }

    @Transactional
    public int assignRolesToPermission(Long permissionId, List<Long> roleIds) {
        return rolePermissionRepository.batchAssignRolesToPermission(permissionId, roleIds);
    }

    @Transactional
    public int removeRolesFromPermission(Long permissionId, List<Long> roleIds) {
        return rolePermissionRepository.batchRemoveRolesFromPermission(permissionId, roleIds);
    }

    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return rolePermissionRepository.findPermissionIdsByRoleId(roleId);
    }

    public List<String> getRoleNamesByPermissionId(Long permissionId) {
        return rolePermissionRepository.findRoleNamesByPermissionId(permissionId);
    }
}
