package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.infrastructure.repository.dataobject.QrcodeDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 二维码仓储转换器
 * <p>
 * 负责 Qrcode 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class QrcodeRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public QrcodeDO toDataObject(Qrcode entity) {
        if (entity == null) {
            return null;
        }
        return QrcodeDO.builder()
                .id(entity.getId())
                .fileId(entity.getFileId())
                .type(entity.getType())
                .epoch(entity.getEpoch())
                .direction(entity.getDirection())
                .isShared(entity.getIsShared())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Qrcode toEntity(QrcodeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Qrcode.reconstruct(
                dataObject.getId(),
                dataObject.getFileId(),
                dataObject.getType(),
                dataObject.getEpoch(),
                dataObject.getDirection(),
                dataObject.getIsShared());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Qrcode> toEntityList(List<QrcodeDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
