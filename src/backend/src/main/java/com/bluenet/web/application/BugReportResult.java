package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;

import java.util.List;

/**
 * Bug 报告聚合的应用层结果对象。
 * <p>
 * 封装了 Bug 报告相关操作返回给 API 层的数据。
 * </p>
 */
public final class BugReportResult {

    private BugReportResult() {
        // 工具类，禁止实例化
    }

    /**
     * Bug 报告创建结果。
     */
    public record Created(
            /** 唯一标识 */
            Long id,
            /** 状态 */
            BugReportStatus status,
            /** GitHub Issue URL（异步同步完成后可通过详情接口获取） */
            String githubIssueUrl) {
    }

    /**
     * Bug 报告详情结果。
     */
    public record Detail(
            /** 唯一标识 */
            Long id,
            /** Bug 标题 */
            String title,
            /** Bug 描述 */
            String description,
            /** 发生页面 URL */
            String pageUrl,
            /** 环境信息 JSON */
            String environmentJson,
            /** 报告者邮箱 */
            String reporterEmail,
            /** 状态 */
            BugReportStatus status,
            /** GitHub Issue URL */
            String githubIssueUrl,
            /** GitHub Issue 编号 */
            Integer githubIssueNumber,
            /** 关联图片文件 ID 列表 */
            List<Long> fileIds) {
    }

    /**
     * Bug 报告列表摘要结果。
     */
    public record Brief(
            /** 唯一标识 */
            Long id,
            /** Bug 标题 */
            String title,
            /** Bug 描述（摘要） */
            String description,
            /** 发生页面 URL */
            String pageUrl,
            /** 报告者邮箱 */
            String reporterEmail,
            /** 状态 */
            BugReportStatus status,
            /** 关联图片数量 */
            int imageCount,
            /** GitHub Issue URL */
            String githubIssueUrl) {
    }
}
