package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.infrastructure.repository.dataobject.EquipmentDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备仓储转换器
 * <p>
 * 负责 Equipment 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class EquipmentRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public EquipmentDO toDataObject(Equipment entity) {
        if (entity == null) {
            return null;
        }
        return EquipmentDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(entity.getBrand())
                .description(entity.getDescription())
                .imageFileId(entity.getImageFileId())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Equipment toEntity(EquipmentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Equipment.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getBrand(),
                dataObject.getDescription(),
                dataObject.getImageFileId(),
                dataObject.getSortOrder());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Equipment> toEntityList(List<EquipmentDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
