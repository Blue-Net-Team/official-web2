package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bug 报告图片值对象/关联实体
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BugReportImage {

    private Long id;
    private Long bugReportId;
    private Long fileId;

    /**
     * 构造新关联
     */
    public static BugReportImage create(Long bugReportId, Long fileId) {
        return new BugReportImage(null, bugReportId, fileId);
    }

    /**
     * 从数据库重建
     */
    public static BugReportImage reconstruct(Long id, Long bugReportId, Long fileId) {
        return new BugReportImage(id, bugReportId, fileId);
    }
}
