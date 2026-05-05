package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.entity.BugReportImage;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportDO;
import com.bluenet.web.infrastructure.repository.dataobject.BugReportImageDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bug 报告仓储转换器
 * <p>
 * 负责 BugReport 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class BugReportRepositoryConverter {

    /**
     * Entity → DO（用于保存/更新）
     */
    public BugReportDO toDataObject(BugReport entity) {
        if (entity == null) {
            return null;
        }
        return BugReportDO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .pageUrl(entity.getPageUrl())
                .environmentJson(entity.getEnvironmentJson())
                .reporterEmail(entity.getReporterEmail())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * DO → Entity（从数据库重建，跳过创建校验）
     */
    public BugReport toEntity(BugReportDO dataObject, List<BugReportImage> images) {
        if (dataObject == null) {
            return null;
        }
        return BugReport.reconstruct(
                dataObject.getId(),
                dataObject.getDescription(),
                dataObject.getPageUrl(),
                dataObject.getEnvironmentJson(),
                dataObject.getReporterEmail(),
                dataObject.getStatus(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt(),
                images);
    }

    /**
     * Image Entity → Image DO
     */
    public BugReportImageDO toImageDataObject(BugReportImage image) {
        if (image == null) {
            return null;
        }
        return BugReportImageDO.builder()
                .id(image.getId())
                .bugReportId(image.getBugReportId())
                .fileId(image.getFileId())
                .build();
    }

    /**
     * Image DO → Image Entity
     */
    public BugReportImage toImageEntity(BugReportImageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return BugReportImage.reconstruct(
                dataObject.getId(),
                dataObject.getBugReportId(),
                dataObject.getFileId());
    }

    /**
     * Image DO 列表 → Image Entity 列表
     */
    public List<BugReportImage> toImageEntityList(List<BugReportImageDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toImageEntity)
                .toList();
    }
}
