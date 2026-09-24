package com.bluenet.web.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.ivencn.infra.rbac.model.PermissionDefinition;
import io.github.ivencn.infra.rbac.spi.PermissionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.infrastructure.repository.converter.PermissionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;

/**
 * iven 框架 {@link PermissionRegistry} 的应用侧实现：将扫描结果同步到 {@code tb_permission}
 * 表，支持新增、更新与幽灵数据物理删除（级联清理角色权限关联）。
 */
@Slf4j
@RequiredArgsConstructor
public class DatabasePermissionRegistry implements PermissionRegistry {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionRepositoryConverter permissionRepositoryConverter;

    @Override
    public void sync(List<PermissionDefinition> definitions) {
        List<Permission> existingPermissions = permissionMapper.selectList(null)
                .stream()
                .map(permissionRepositoryConverter::toEntity)
                .toList();
        Map<String, Permission> existingMap = existingPermissions.stream()
                .collect(Collectors.toMap(Permission::getValue, p -> p, (p1, p2) -> p1));

        java.util.Set<String> scannedValues = definitions.stream()
                .map(PermissionDefinition::value)
                .collect(Collectors.toSet());

        List<PermissionDefinition> toInsert = new java.util.ArrayList<>();
        List<PermissionDefinition> toUpdate = new java.util.ArrayList<>();
        List<Permission> toDelete = new java.util.ArrayList<>();

        for (PermissionDefinition def : definitions) {
            Permission existing = existingMap.get(def.value());
            if (existing == null) {
                toInsert.add(def);
            } else if (!isSame(def, existing)) {
                toUpdate.add(def);
            }
        }

        for (Permission existing : existingPermissions) {
            if (!scannedValues.contains(existing.getValue())) {
                toDelete.add(existing);
            }
        }

        performDatabaseOperations(toInsert, toUpdate, toDelete);

        log.info(
                "Permission sync: {} new, {} updated, {} deleted (ghost)",
                toInsert.size(),
                toUpdate.size(),
                toDelete.size());
    }

    private boolean isSame(PermissionDefinition def, Permission existing) {
        return java.util.Objects.equals(def.name(), existing.getName())
                && java.util.Objects.equals(def.url(), existing.getUrl())
                && java.util.Objects.equals(def.method(), existing.getMethod())
                && java.util.Objects.equals(def.accessLevel().name(), existing.getAccessLevel());
    }

    private void performDatabaseOperations(List<PermissionDefinition> toInsert, List<PermissionDefinition> toUpdate,
            List<Permission> toDelete) {
        for (PermissionDefinition def : toInsert) {
            PermissionDO permission = new PermissionDO();
            permission.setValue(def.value());
            permission.setName(def.name());
            permission.setUrl(def.url());
            permission.setMethod(def.method());
            permission.setAccessLevel(def.accessLevel().name());
            permissionMapper.insert(permission);
        }

        for (PermissionDefinition def : toUpdate) {
            PermissionDO existing = permissionMapper
                    .selectOne(
                            new LambdaQueryWrapper<PermissionDO>()
                                    .eq(PermissionDO::getValue, def.value()));
            if (existing != null) {
                existing.setName(def.name());
                existing.setUrl(def.url());
                existing.setMethod(def.method());
                existing.setAccessLevel(def.accessLevel().name());
                permissionMapper.updateById(existing);
            }
        }

        // 物理删除幽灵数据（同时级联删除 role_permission 关联）
        for (Permission ghost : toDelete) {
            rolePermissionMapper.delete(
                    new LambdaQueryWrapper<RolePermissionDO>()
                            .eq(RolePermissionDO::getPermissionId, ghost.getId()));
            permissionMapper.deleteById(ghost.getId());
        }
    }
}
