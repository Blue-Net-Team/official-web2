package com.bluenet.web.application.query.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;

/**
 * 查询 Bug 报告列表查询参数。
 */
public record GetBugReportListQuery(
        /** 页码 */
        Integer page,
        /** 每页大小 */
        Integer size,
        /** 状态筛选 */
        BugReportStatus status) {
}
