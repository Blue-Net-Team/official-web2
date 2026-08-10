package com.bluenet.web.application.query.auditstatistics;

import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;

/**
 * 获取接口调用排行查询参数。
 */
public record GetEndpointRankingQuery(
        /** 周期 */
        AuditStatisticsPeriod period,
        /** 限制数量 */
        int limit) {
}
