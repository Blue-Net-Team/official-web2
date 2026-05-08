package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件仓储转换器
 * <p>
 * 负责 File 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class FileRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public FileDO toDataObject(File entity) {
        if (entity == null) {
            return null;
        }
        return FileDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .url(entity.getUrl())
                .status(entity.getStatus())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public File toEntity(FileDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return File.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getType(),
                dataObject.getUrl(),
                dataObject.getStatus());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<File> toEntityList(List<FileDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
