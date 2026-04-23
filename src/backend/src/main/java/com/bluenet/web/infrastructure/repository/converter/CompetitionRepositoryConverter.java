package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 竞赛仓储转换器
 * <p>
 * 负责 Competition 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class CompetitionRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public CompetitionDO toDataObject(Competition entity) {
        if (entity == null) {
            return null;
        }
        return CompetitionDO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .logoFileId(entity.getLogoFileId())
                .coverFileId(entity.getCoverFileId())
                .summary(entity.getSummary())
                .level(entity.getLevel())
                .month(entity.getMonth())
                .organizer(entity.getOrganizer())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public Competition toEntity(CompetitionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Competition.reconstruct(
                dataObject.getId(),
                dataObject.getName(),
                dataObject.getShortName(),
                dataObject.getLogoFileId(),
                dataObject.getCoverFileId(),
                dataObject.getSummary(),
                dataObject.getLevel(),
                dataObject.getMonth(),
                dataObject.getOrganizer(),
                dataObject.getSortOrder());
    }

    /**
     * DO 列表 → Entity 列表
     */
    public List<Competition> toEntityList(List<CompetitionDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toEntity)
                .toList();
    }
}
