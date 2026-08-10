package com.bluenet.web.application.service;

import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.command.bugreport.BugReportCommands;

/**
 * Bug 报告应用服务接口（公开端）。
 * <p>
 * 定义了 Bug 报告聚合在应用层的公开业务操作。
 * </p>
 */
public interface BugReportAppService {

    /**
     * 提交 Bug 报告
     *
     * @param command
     *            创建命令
     * @return 创建后的报告结果
     */
    BugReportResult.Created submitBugReport(BugReportCommands.CreateBugReportCommand command);
}
