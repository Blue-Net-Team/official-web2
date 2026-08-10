package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Bug 报告聚合根
 * <p>
 * 承载 Bug 报告相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BugReport {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_IMAGES = 3;

    private Long id;
    private String title;
    private String description;
    private String pageUrl;
    private String environmentJson;
    private String reporterEmail;
    private BugReportStatus status;
    private String githubIssueUrl;
    private Integer githubIssueNumber;
    private List<BugReportImage> images = new ArrayList<>();

    private BugReport(Long id, String title, String description, String pageUrl, String environmentJson,
            String reporterEmail, BugReportStatus status,
            String githubIssueUrl, Integer githubIssueNumber, List<BugReportImage> images) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pageUrl = pageUrl;
        this.environmentJson = environmentJson;
        this.reporterEmail = reporterEmail;
        this.status = status;
        this.githubIssueUrl = githubIssueUrl;
        this.githubIssueNumber = githubIssueNumber;
        this.images = images != null ? images : new ArrayList<>();
    }

    /**
     * 构造新 Bug 报告 —— 带领域校验
     */
    public static BugReport create(String title, String description, String pageUrl,
            String environmentJson, String reporterEmail, List<Long> fileIds) {
        title = requireNonBlankMaxLength(title, "标题", MAX_TITLE_LENGTH);
        description = requireNonBlankMaxLength(description, "描述", MAX_DESCRIPTION_LENGTH);
        if (fileIds != null && fileIds.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("最多上传 " + MAX_IMAGES + " 张截图");
        }

        List<BugReportImage> imageList = new ArrayList<>();
        if (fileIds != null) {
            for (Long fileId : fileIds) {
                imageList.add(BugReportImage.create(null, fileId));
            }
        }

        return new BugReport(null, title, description, pageUrl,
                environmentJson, reporterEmail, BugReportStatus.PENDING,
                null, null, imageList);
    }

    private static String requireNonBlankMaxLength(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bug " + fieldName + "不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException("Bug " + fieldName + "最多 " + maxLength + " 字符");
        }
        return value.trim();
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static BugReport reconstruct(Long id, String title, String description, String pageUrl,
            String environmentJson, String reporterEmail, BugReportStatus status,
            String githubIssueUrl, Integer githubIssueNumber, List<BugReportImage> images) {
        return new BugReport(id, title, description, pageUrl, environmentJson,
                reporterEmail, status, githubIssueUrl, githubIssueNumber, images);
    }

    /**
     * 更新状态
     */
    public void updateStatus(BugReportStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("状态不能为空");
        }
        this.status = newStatus;
    }

    /**
     * 更新 GitHub Issue 同步结果
     */
    public void updateGithubIssueInfo(String url, Integer number) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("GitHub Issue URL 不能为空");
        }
        if (number == null || number <= 0) {
            throw new IllegalArgumentException("GitHub Issue 编号必须大于 0");
        }
        this.githubIssueUrl = url;
        this.githubIssueNumber = number;
    }
}
