package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.permission.PermissionRoleResponseDTO;
import com.bluenet.web.api.dto.permission.RolePermissionResponseDTO;
import com.bluenet.web.application.service.RolePermissionManageService;
import com.bluenet.web.domain.model.vo.PermissionVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.service.RolePermissionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionManageServiceImpl implements RolePermissionManageService {

    private final RolePermissionDomainService domainService;
    private final PermissionRepository permissionRepository;

    @Override
    public List<String> getRolePermissions(String roleName) {
        RoleVO role = domainService.resolveRoleIncludeSuperAdmin(roleName);
        List<Long> permissionIds = domainService.getPermissionIdsByRoleId(role.getId());
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        List<PermissionVO> permissions = permissionRepository.findAllByIds(permissionIds);
        return permissions.stream()
                .map(PermissionVO::getValue)
                .sorted()
                .toList();
    }

    @Override
    @Transactional
    public RolePermissionResponseDTO assignPermissionsToRole(String roleName, List<Long> permissionIds) {
        RoleVO role = domainService.resolveRole(roleName);
        List<Long> validatedIds = domainService.resolvePermissionIds(permissionIds);
        int count = domainService.assignPermissionsToRole(role.getId(), validatedIds);
        List<String> currentValues = resolvePermissionValues(domainService.getPermissionIdsByRoleId(role.getId()));
        return RolePermissionResponseDTO.builder()
                .successCount(count)
                .currentPermissions(currentValues)
                .build();
    }

    @Override
    @Transactional
    public RolePermissionResponseDTO removePermissionsFromRole(String roleName, List<Long> permissionIds) {
        RoleVO role = domainService.resolveRole(roleName);
        List<Long> validatedIds = domainService.resolvePermissionIds(permissionIds);
        int count = domainService.removePermissionsFromRole(role.getId(), validatedIds);
        List<String> currentValues = resolvePermissionValues(domainService.getPermissionIdsByRoleId(role.getId()));
        return RolePermissionResponseDTO.builder()
                .successCount(count)
                .currentPermissions(currentValues)
                .build();
    }

    @Override
    public List<String> getPermissionRoles(Long permissionId) {
        return domainService.getRoleNamesByPermissionId(permissionId);
    }

    @Override
    @Transactional
    public PermissionRoleResponseDTO assignRolesToPermission(Long permissionId, List<String> roleNames) {
        List<Long> roleIds = domainService.resolveRoleIds(roleNames);
        int count = domainService.assignRolesToPermission(permissionId, roleIds);
        List<String> currentRoles = domainService.getRoleNamesByPermissionId(permissionId);
        return PermissionRoleResponseDTO.builder()
                .successCount(count)
                .currentRoles(currentRoles)
                .build();
    }

    @Override
    @Transactional
    public PermissionRoleResponseDTO removeRolesFromPermission(Long permissionId, List<String> roleNames) {
        List<Long> roleIds = domainService.resolveRoleIds(roleNames);
        int count = domainService.removeRolesFromPermission(permissionId, roleIds);
        List<String> currentRoles = domainService.getRoleNamesByPermissionId(permissionId);
        return PermissionRoleResponseDTO.builder()
                .successCount(count)
                .currentRoles(currentRoles)
                .build();
    }

    private List<String> resolvePermissionValues(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }
        List<PermissionVO> permissions = permissionRepository.findAllByIds(permissionIds);
        return permissions.stream()
                .map(PermissionVO::getValue)
                .sorted()
                .toList();
    }
}
