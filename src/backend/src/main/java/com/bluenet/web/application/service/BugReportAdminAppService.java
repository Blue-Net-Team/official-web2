package com.bluenet.web.application.service;

import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.query.bugreport.GetBugReportListQuery;
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
     * @param query
     *            查询参数
     * @return 分页后的报告摘要结果
     */
    Page<BugReportResult.Brief> getBugReportList(GetBugReportListQuery query);

    /**
     * 获取 Bug 报告详情
     *
     * @param id
     *            报告 ID
     * @return 报告详情结果
     */
    BugReportResult.Detail getBugReportDetail(Long id);

}
