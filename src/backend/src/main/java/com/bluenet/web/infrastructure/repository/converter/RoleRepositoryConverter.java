package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色仓储转换器
 * <p>
 * 负责 Role 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class RoleRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public RoleDO toDataObject(Role entity) {
        if (entity == null) {
            return null;
        }
        return RoleDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Role toEntity(RoleDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Role.reconstruct(dataObject.getId(), dataObject.getName());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Role> toEntityList(List<RoleDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
