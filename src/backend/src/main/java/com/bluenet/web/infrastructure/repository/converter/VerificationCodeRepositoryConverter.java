package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.infrastructure.repository.dataobject.VerifyCodeDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 验证码仓储转换器
 * <p>
 * 负责 VerifyCode 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class VerificationCodeRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public VerifyCodeDO toDataObject(VerifyCode entity) {
        if (entity == null) {
            return null;
        }
        return VerifyCodeDO.builder()
                .id(entity.getId())
                .target(entity.getTarget())
                .code(entity.getCode())
                .expireAt(entity.getExpireAt())
                .usedAt(entity.getUsedAt())
                .scene(entity.getScene())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public VerifyCode toEntity(VerifyCodeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return VerifyCode.reconstruct(
                dataObject.getId(),
                dataObject.getTarget(),
                dataObject.getCode(),
                dataObject.getExpireAt(),
                dataObject.getUsedAt(),
                dataObject.getScene());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<VerifyCode> toEntityList(List<VerifyCodeDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
