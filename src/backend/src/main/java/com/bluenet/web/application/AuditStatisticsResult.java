package com.bluenet.web.application;

import java.time.LocalDateTime;

/**
 * 审计统计聚合的应用层结果对象。
 * <p>
 * 封装了审计统计相关操作返回给 API 层的数据。
 * </p>
 */
public final class AuditStatisticsResult {

    private AuditStatisticsResult() {
        // 工具类，禁止实例化
    }

    /**
     * 趋势点数据。
     */
    public record TrendPoint(
            /** 时间 */
            LocalDateTime time,
            /** 数量 */
            Long count) {
    }

    /**
     * 端点排名数据。
     */
    public record EndpointRanking(
            /** 路径模式 */
            String pattern,
            /** 请求数量 */
            Long count,
            /** 平均耗时（毫秒） */
            Double avgDurationMs,
            /** 错误数量 */
            Long errorCount) {
    }

    /**
     * 端点延迟数据。
     */
    public record EndpointLatency(
            /** 路径模式 */
            String pattern,
            /** 平均耗时（毫秒） */
            Double avgDurationMs,
            /** 最大耗时（毫秒） */
            Long maxDurationMs,
            /** 请求数量 */
            Long count) {
    }
}
