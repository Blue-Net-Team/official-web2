package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.infrastructure.repository.dataobject.CollegeDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学院仓储转换器
 * <p>
 * 负责 College 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class CollegeRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public CollegeDO toDataObject(College entity) {
        if (entity == null) {
            return null;
        }
        return CollegeDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public College toEntity(CollegeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return College.reconstruct(dataObject.getId(), dataObject.getName());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<College> toEntityList(List<CollegeDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
