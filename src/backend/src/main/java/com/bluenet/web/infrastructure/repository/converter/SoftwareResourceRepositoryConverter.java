package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 软件资源仓储转换器。
 * <p>
 * 负责 SoftwareResource 的 DO 与 Entity 之间的显式字段映射。
 * </p>
 */
@Component
public class SoftwareResourceRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public SoftwareResourceDO toDataObject(SoftwareResource entity) {
        if (entity == null) {
            return null;
        }
        return SoftwareResourceDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .direction(entity.getDirection())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .externalUrl(entity.getExternalUrl())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public SoftwareResource toEntity(SoftwareResourceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return SoftwareResource.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getDirection(),
                dataObject.getCategory(),
                dataObject.getDescription(),
                dataObject.getExternalUrl(),
                dataObject.getSortOrder(),
                dataObject.getStatus());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<SoftwareResource> toEntityList(List<SoftwareResourceDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
