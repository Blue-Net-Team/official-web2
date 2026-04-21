package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;

    /**
     * 查询拥有任一指定权限的角色名称集合。
     *
     * @param permissionIds
     *            权限主键集合。
     * @return 满足条件的角色权限关系 结果集合。
     */
    @Override
    public Map<Long, List<String>> findRoleNamesByPermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<RolePermission> rolePermissions = RepositoryObjectConverter.toDomainList(
                rolePermissionMapper.selectByPermissionIds(permissionIds),
                RolePermission.class);

        List<Long> roleIds = rolePermissions.stream()
                .map(RolePermission::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> roleIdToNameMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = RepositoryObjectConverter.toDomainList(roleMapper.selectBatchIds(roleIds), Role.class);
            for (Role role : roles) {
                roleIdToNameMap.put(role.getId(), role.getName());
            }
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (RolePermission rp : rolePermissions) {
            Long permissionId = rp.getPermissionId();
            Long roleId = rp.getRoleId();
            String roleName = roleIdToNameMap.get(roleId);

            if (roleName != null) {
                result.computeIfAbsent(permissionId, k -> new ArrayList<>()).add(roleName);
            }
        }

        return result;
    }

    /**
     * 查询拥有指定权限的角色名称集合。
     *
     * @param permissionId
     *            权限主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    @Override
    public List<String> findRoleNamesByPermissionId(Long permissionId) {
        Map<Long, List<String>> map = findRoleNamesByPermissionIds(List.of(permissionId));
        return map.getOrDefault(permissionId, List.of());
    }

    /**
     * 查询角色已绑定的权限主键集合。
     *
     * @param roleId
     *            角色主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 查询拥有指定权限的角色主键集合。
     *
     * @param permissionId
     *            权限主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    @Override
    public List<Long> findRoleIdsByPermissionId(Long permissionId) {
        return rolePermissionMapper.selectRoleIdsByPermissionId(permissionId);
    }

    /**
     * 判断角色与权限是否已经存在授权关系。
     *
     * @param roleId
     *            角色主键。
     * @param permissionId
     *            权限主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return rolePermissionMapper.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    /**
     * 批量为角色授予权限。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 数据库受影响行数。
     */
    @Override
    public int batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        List<RolePermission> existing = filterExistingAssignments(roleId, permissionIds);
        List<Long> newPermissionIds = permissionIds.stream()
                .filter(pid -> existing.stream().noneMatch(rp -> rp.getPermissionId().equals(pid)))
                .collect(Collectors.toList());

        if (newPermissionIds.isEmpty()) {
            return 0;
        }

        // 批量插入仍通过 Mapper 的 DO 入参完成，避免暴露领域实体给持久层。
        List<RolePermissionDO> toInsert = newPermissionIds.stream()
                .map(pid -> {
                    RolePermissionDO rp = new RolePermissionDO();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(pid);
                    return rp;
                })
                .collect(Collectors.toList());

        rolePermissionMapper.batchInsert(toInsert);
        return newPermissionIds.size();
    }

    /**
     * 批量移除角色已授予的权限。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 数据库受影响行数。
     */
    @Override
    public int batchRemovePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return 0;
        }
        return rolePermissionMapper.batchDelete(roleId, permissionIds);
    }

    /**
     * 批量为权限绑定可访问角色。
     *
     * @param permissionId
     *            权限主键。
     * @param roleIds
     *            角色主键集合。
     * @return 数据库受影响行数。
     */
    @Override
    public int batchAssignRolesToPermission(Long permissionId, List<Long> roleIds) {
        int count = 0;
        for (Long roleId : roleIds) {
            if (!existsByRoleIdAndPermissionId(roleId, permissionId)) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                RepositoryObjectConverter.insert(rolePermissionMapper, rp, RolePermissionDO.class);
                count++;
            }
        }
        return count;
    }

    /**
     * 批量移除权限与角色的绑定关系。
     *
     * @param permissionId
     *            权限主键。
     * @param roleIds
     *            角色主键集合。
     * @return 数据库受影响行数。
     */
    @Override
    public int batchRemoveRolesFromPermission(Long permissionId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return 0;
        }
        return rolePermissionMapper.batchDeleteByPermissionId(permissionId, roleIds);
    }

    /**
     * 过滤已经存在的角色权限关系，避免重复插入。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 满足条件的角色权限关系 结果集合。
     */
    private List<RolePermission> filterExistingAssignments(Long roleId, List<Long> permissionIds) {
        return RepositoryObjectConverter.toDomainList(
                rolePermissionMapper.selectByRoleIdAndPermissionIds(roleId, permissionIds),
                RolePermission.class);
    }
}
