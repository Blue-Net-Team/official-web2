package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.PermissionResult;
import com.bluenet.web.application.command.permission.PermissionCommands;
import com.bluenet.web.application.service.PermissionAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限应用服务实现。
 * <p>
 * 实现权限聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PermissionAppServiceImpl implements PermissionAppService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 分页查询权限列表。
     *
     * @param command
     *            查询权限命令
     * @return 权限分页结果
     */
    @Override
    public Page<PermissionResult> getPermissions(PermissionCommands.GetPermissionsCommand command) {
        int pageNum = command.page() != null ? command.page() : 0;
        int pageSize = command.size() != null ? command.size() : 20;
        pageSize = Math.min(Math.max(pageSize, 1), 100);

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Permission> permissionPage = permissionRepository.findAll(
                command.keyword(),
                command.format(),
                pageable);

        if (permissionPage.getContent().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, permissionPage.getTotalElements());
        }

        List<Long> permissionIds = permissionPage.getContent()
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toList());
        Map<Long, List<String>> rolesMap = rolePermissionRepository.findRoleNamesByPermissionIds(permissionIds);

        List<PermissionResult> content = permissionPage.getContent()
                .stream()
                .map(permission -> toResult(permission, rolesMap.getOrDefault(permission.getId(), List.of())))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, permissionPage.getTotalElements());
    }

    /**
     * 根据ID查询权限详情。
     *
     * @param id
     *            权限ID
     * @return 权限详情结果
     */
    @Override
    public PermissionResult getPermissionDetail(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("权限不存在"));

        List<String> assignedRoles = rolePermissionRepository.findRoleNamesByPermissionId(id);
        return toResult(permission, assignedRoles);
    }

    /**
     * 查询权限树。
     *
     * @return 权限结果列表
     */
    @Override
    public List<PermissionResult> getPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        return allPermissions.stream()
                .map(permission -> toResult(permission, List.of()))
                .collect(Collectors.toList());
    }

    private PermissionResult toResult(Permission permission, List<String> assignedRoles) {
        return new PermissionResult(
                permission.getId(),
                permission.getValue(),
                permission.getName(),
                permission.getUrl(),
                permission.getMethod(),
                permission.getAccessLevel(),
                assignedRoles);
    }
}
