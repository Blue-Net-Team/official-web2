package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.infrastructure.repository.dataobject.UserExperienceDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户经历仓储转换器
 * <p>
 * 负责 UserExperience 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class UserExperienceRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public UserExperienceDO toDataObject(UserExperience entity) {
        if (entity == null) {
            return null;
        }
        return UserExperienceDO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public UserExperience toEntity(UserExperienceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return UserExperience.reconstruct(
                dataObject.getId(),
                dataObject.getUserId(),
                dataObject.getType(),
                dataObject.getTitle(),
                dataObject.getContent(),
                dataObject.getStartTime(),
                dataObject.getEndTime());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<UserExperience> toEntityList(List<UserExperienceDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
