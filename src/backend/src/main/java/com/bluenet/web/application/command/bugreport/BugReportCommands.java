package com.bluenet.web.application.command.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;

import java.util.List;

/**
 * Bug 报告聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class BugReportCommands {

    /** 禁止实例化。 */
    private BugReportCommands() {
    }

    /**
     * 创建 Bug 报告命令。
     * <p>
     * 用于提交新的 Bug 报告。
     * </p>
     */
    public record CreateBugReportCommand(
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
            /** 关联图片文件 ID 列表 */
            List<Long> fileIds) {
        public CreateBugReportCommand {
            if (title != null) {
                title = title.trim();
            }
            if (description != null) {
                description = description.trim();
            }
            if (pageUrl != null) {
                pageUrl = pageUrl.trim();
            }
            if (reporterEmail != null) {
                reporterEmail = reporterEmail.trim();
            }
        }
    }

    /**
     * 查询 Bug 报告列表命令。
     * <p>
     * 用于管理员分页查询 Bug 报告列表。
     * </p>
     */
    public record GetBugReportListCommand(
            /** 页码 */
            Integer page,
            /** 每页大小 */
            Integer size,
            /** 状态筛选 */
            BugReportStatus status) {
    }
}
