package com.bluenet.web.infrastructure.security.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import jakarta.annotation.PostConstruct;

/**
 * 权限缓存 启动时加载权限数据到内存，避免每次请求查询数据库
 */
@Component
public class PermissionCache {

    /**
     * 权限值 -> Permission 对象
     */
    private final Map<String, Permission> permissionMap = new ConcurrentHashMap<>();

    /**
     * 角色ID -> 权限值集合
     */
    private final Map<Long, Set<String>> rolePermissionMap = new ConcurrentHashMap<>();

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionCache(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 刷新缓存
     */
    public void refresh() {
        // 加载所有权限
        List<Permission> permissions = permissionMapper.selectList(null)
                .stream()
                // 缓存对外仍暴露领域 Permission，Mapper 边界只返回 PermissionDO。
                .map(permission -> RepositoryObjectConverter.copy(permission, Permission.class))
                .toList();
        permissionMap.clear();
        permissions.forEach(p -> permissionMap.put(p.getValue(), p));

        // 加载所有角色权限关系
        List<RolePermissionDO> rolePermissions = rolePermissionMapper.selectList(null);
        rolePermissionMap.clear();
        rolePermissions.forEach(rp -> {
            rolePermissionMap.computeIfAbsent(rp.getRoleId(), k -> ConcurrentHashMap.newKeySet())
                    .add(getPermissionValueById(rp.getPermissionId()));
        });
    }

    /**
     * 获取所有权限
     */
    public Collection<Permission> getAllPermissions() {
        return permissionMap.values();
    }

    /**
     * 根据权限值获取权限
     */
    public Permission getByValue(String value) {
        return permissionMap.get(value);
    }

    /**
     * 获取所有权限值
     */
    public Set<String> getAllPermissionValues() {
        return new HashSet<>(permissionMap.keySet());
    }

    /**
     * 检查权限是否存在
     */
    public boolean exists(String value) {
        return permissionMap.containsKey(value);
    }

    /**
     * 获取角色的所有权限值
     */
    public Set<String> getPermissionsByRole(Long roleId) {
        return rolePermissionMap.getOrDefault(roleId, Collections.emptySet());
    }

    /**
     * 检查角色是否有指定权限
     */
    public boolean hasPermission(Long roleId, String permissionValue) {
        Set<String> permissions = rolePermissionMap.get(roleId);
        return permissions != null && permissions.contains(permissionValue);
    }

    /**
     * 检查权限是否有角色关联
     *
     * @return true 如果没有任何角色关联此权限（孤儿权限）
     */
    public boolean isOrphan(String permissionValue) {
        Permission permission = permissionMap.get(permissionValue);
        if (permission == null) {
            return true;
        }

        return rolePermissionMap.values().stream().noneMatch(set -> set.contains(permissionValue));
    }

    /**
     * 根据权限ID获取权限值
     */
    private String getPermissionValueById(Long permissionId) {
        return permissionMap.values()
                .stream()
                .filter(p -> p.getId().equals(permissionId))
                .map(Permission::getValue)
                .findFirst()
                .orElse(null);
    }
}
