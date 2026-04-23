package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.infrastructure.repository.dataobject.VenueDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 场地仓储转换器
 * <p>
 * 负责 Venue 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class VenueRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public VenueDO toDataObject(Venue entity) {
        if (entity == null) {
            return null;
        }
        return VenueDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subtitle(entity.getSubtitle())
                .description(entity.getDescription())
                .imageFileId(entity.getImageFileId())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Venue toEntity(VenueDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Venue.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getSubtitle(),
                dataObject.getDescription(),
                dataObject.getImageFileId(),
                dataObject.getSortOrder());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Venue> toEntityList(List<VenueDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
