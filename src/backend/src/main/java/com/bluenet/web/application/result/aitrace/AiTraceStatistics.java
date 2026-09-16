package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 对话统计聚合结果。
 *
 * <p>
 * 趋势按会话聚合，分布类指标按提问聚合。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTraceStatistics {
    /**
     * 概览指标。
     */
    private AiTraceOverview overview;
    /**
     * 会话量趋势（按会话聚合，时间桶补零）。
     */
    private List<AiTrendPoint> trend;
    /**
     * 意图分布（按提问聚合）。
     */
    private List<AiCountItem> intents;
    /**
     * 动作分布（按提问聚合）。
     */
    private List<AiCountItem> actions;
    /**
     * 检索工具使用分布。
     */
    private List<AiCountItem> tools;
    /**
     * 拒答原因分布。
     */
    private List<AiCountItem> refusals;
}
