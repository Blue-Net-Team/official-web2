package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.RolePermission;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色权限关联仓储转换器
 * <p>
 * 负责 RolePermission 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class RolePermissionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public RolePermissionDO toDataObject(RolePermission entity) {
        if (entity == null) {
            return null;
        }
        return RolePermissionDO.builder()
                .id(entity.getId())
                .roleId(entity.getRoleId())
                .permissionId(entity.getPermissionId())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public RolePermission toEntity(RolePermissionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return RolePermission.reconstruct(
                dataObject.getId(),
                dataObject.getRoleId(),
                dataObject.getPermissionId());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<RolePermission> toEntityList(List<RolePermissionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
