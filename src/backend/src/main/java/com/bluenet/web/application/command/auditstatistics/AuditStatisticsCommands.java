package com.bluenet.web.application.command.auditstatistics;

import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;

/**
 * 审计统计聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class AuditStatisticsCommands {

    /** 禁止实例化。 */
    private AuditStatisticsCommands() {
    }

    /**
     * 获取趋势命令。
     * <p>
     * 用于获取审计统计趋势数据。
     * </p>
     */
    public record GetTrendsCommand(
            /** 周期 */
            AuditStatisticsPeriod period) {
    }

    /**
     * 获取接口排行命令。
     * <p>
     * 用于获取接口调用量排行。
     * </p>
     */
    public record GetEndpointRankingCommand(
            /** 周期 */
            AuditStatisticsPeriod period,
            /** 限制数量 */
            int limit) {
    }

    /**
     * 获取接口延迟排行命令。
     * <p>
     * 用于获取接口延迟排行。
     * </p>
     */
    public record GetEndpointLatencyRankingCommand(
            /** 周期 */
            AuditStatisticsPeriod period,
            /** 限制数量 */
            int limit) {
    }
}
