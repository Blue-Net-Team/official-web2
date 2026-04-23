package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限仓储转换器
 * <p>
 * 负责 Permission 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class PermissionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public PermissionDO toDataObject(Permission entity) {
        if (entity == null) {
            return null;
        }
        return PermissionDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .value(entity.getValue())
                .url(entity.getUrl())
                .method(entity.getMethod())
                .accessLevel(entity.getAccessLevel())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Permission toEntity(PermissionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Permission.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getValue(),
                dataObject.getUrl(),
                dataObject.getMethod(),
                dataObject.getAccessLevel());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Permission> toEntityList(List<PermissionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
