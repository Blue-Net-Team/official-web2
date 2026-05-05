package com.bluenet.web.application.service;

import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import org.springframework.data.domain.Page;

/**
 * Bug 报告管理应用服务接口（管理端）。
 * <p>
 * 定义了 Bug 报告聚合在应用层的管理业务操作。
 * </p>
 */
public interface BugReportAdminAppService {

    /**
     * 分页查询 Bug 报告列表
     *
     * @param command
     *            查询命令
     * @return 分页后的报告摘要结果
     */
    Page<BugReportResult.Brief> getBugReportList(BugReportCommands.GetBugReportListCommand command);

    /**
     * 获取 Bug 报告详情
     *
     * @param id
     *            报告 ID
     * @return 报告详情结果
     */
    BugReportResult.Detail getBugReportDetail(Long id);

    /**
     * 更新 Bug 报告状态
     *
     * @param command
     *            更新状态命令
     * @return 更新后的报告详情
     */
    BugReportResult.Detail updateStatus(BugReportCommands.UpdateBugReportStatusCommand command);
}
