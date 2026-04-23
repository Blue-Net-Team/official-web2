package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.infrastructure.repository.converter.RolePermissionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色权限关联仓储实现
 * <p>
 * 实现角色权限关联数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionRepositoryConverter converter;

    @Override
    public Map<Long, List<String>> findRoleNamesByPermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<RolePermission> rolePermissions = converter.toEntityList(
                rolePermissionMapper.selectByPermissionIds(permissionIds));

        List<Long> roleIds = rolePermissions.stream()
                .map(RolePermission::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> roleIdToNameMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<RoleDO> roles = roleMapper.selectBatchIds(roleIds);
            for (RoleDO role : roles) {
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

    @Override
    public List<String> findRoleNamesByPermissionId(Long permissionId) {
        Map<Long, List<String>> map = findRoleNamesByPermissionIds(List.of(permissionId));
        return map.getOrDefault(permissionId, List.of());
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<Long> findRoleIdsByPermissionId(Long permissionId) {
        return rolePermissionMapper.selectRoleIdsByPermissionId(permissionId);
    }

    @Override
    public boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return rolePermissionMapper.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public int batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        List<RolePermission> existing = filterExistingAssignments(roleId, permissionIds);
        List<Long> newPermissionIds = permissionIds.stream()
                .filter(pid -> existing.stream().noneMatch(rp -> rp.getPermissionId().equals(pid)))
                .collect(Collectors.toList());

        if (newPermissionIds.isEmpty()) {
            return 0;
        }

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

    @Override
    public int batchRemovePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return 0;
        }
        return rolePermissionMapper.batchDelete(roleId, permissionIds);
    }

    @Override
    public int batchAssignRolesToPermission(Long permissionId, List<Long> roleIds) {
        int count = 0;
        for (Long roleId : roleIds) {
            if (!existsByRoleIdAndPermissionId(roleId, permissionId)) {
                RolePermission rp = RolePermission.create(roleId, permissionId);
                rolePermissionMapper.insert(converter.toDataObject(rp));
                count++;
            }
        }
        return count;
    }

    @Override
    public int batchRemoveRolesFromPermission(Long permissionId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return 0;
        }
        return rolePermissionMapper.batchDeleteByPermissionId(permissionId, roleIds);
    }

    @Override
    public void save(RolePermission rolePermission) {
        RolePermissionDO dataObject = converter.toDataObject(rolePermission);
        rolePermissionMapper.insert(dataObject);
        rolePermission.setId(dataObject.getId());
    }

    @Override
    public void update(RolePermission rolePermission) {
        RolePermissionDO dataObject = converter.toDataObject(rolePermission);
        rolePermissionMapper.updateById(dataObject);
    }

    @Override
    public void deleteById(Long id) {
        rolePermissionMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return rolePermissionMapper.selectById(id) != null;
    }

    private List<RolePermission> filterExistingAssignments(Long roleId, List<Long> permissionIds) {
        return converter.toEntityList(
                rolePermissionMapper.selectByRoleIdAndPermissionIds(roleId, permissionIds));
    }
}
