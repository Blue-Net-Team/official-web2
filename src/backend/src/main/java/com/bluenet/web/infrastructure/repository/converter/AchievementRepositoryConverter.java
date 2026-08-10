package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.AchievementExternalMember;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementExternalMemberDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 成就仓储转换器
 * <p>
 * 负责 Achievement 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AchievementRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public AchievementDO toDataObject(Achievement entity) {
        if (entity == null) {
            return null;
        }
        return AchievementDO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .relateTo(entity.getRelateTo())
                .achieveAt(entity.getAchieveAt())
                .awardLevel(entity.getAwardLevel())
                .awardName(entity.getAwardName())
                .fileId(entity.getFileId())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Achievement toEntity(AchievementDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Achievement.reconstruct(
                dataObject.getId(),
                dataObject.getTitle(),
                dataObject.getType(),
                dataObject.getRelateTo(),
                dataObject.getAchieveAt(),
                dataObject.getAwardLevel(),
                dataObject.getAwardName(),
                dataObject.getFileId());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Achievement> toEntityList(List<AchievementDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }

    /**
     * 外部协作者 DO → Entity
     */
    public AchievementExternalMember toExternalMemberEntity(AchievementExternalMemberDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        AchievementExternalMember member = new AchievementExternalMember();
        member.setId(dataObject.getId());
        member.setAchievementId(dataObject.getAchievementId());
        member.setName(dataObject.getName());
        member.setDisplayOrder(dataObject.getDisplayOrder());
        return member;
    }

    /**
     * 外部协作者 Entity → DO
     */
    public AchievementExternalMemberDO toExternalMemberDataObject(AchievementExternalMember entity) {
        if (entity == null) {
            return null;
        }
        return AchievementExternalMemberDO.builder()
                .id(entity.getId())
                .achievementId(entity.getAchievementId())
                .name(entity.getName())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
