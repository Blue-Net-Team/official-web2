package com.bluenet.web.application.query.auditstatistics;

import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;

/**
 * 获取审计趋势统计查询参数。
 */
public record GetTrendsQuery(
        /** 周期 */
        AuditStatisticsPeriod period) {
}
