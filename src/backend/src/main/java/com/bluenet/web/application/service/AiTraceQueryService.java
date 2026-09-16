package com.bluenet.web.application.service;

import com.bluenet.web.application.query.aitrace.AiTraceGapQuery;
import com.bluenet.web.application.query.aitrace.AiTraceListQuery;
import com.bluenet.web.application.query.aitrace.AiTraceStatisticsQuery;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.result.aitrace.AiConversationSummary;
import com.bluenet.web.application.result.aitrace.AiCountItem;
import com.bluenet.web.application.result.aitrace.AiTraceOverview;
import com.bluenet.web.application.result.aitrace.AiTraceStatistics;
import com.bluenet.web.application.result.aitrace.AiTrendPoint;
import com.bluenet.web.application.result.aitrace.AiTurnSummary;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * AI 对话轨迹查询应用服务。
 *
 * <p>
 * 全部操作为只读查询：轨迹由 ai-service 写入，本服务不提供任何写入能力。
 * </p>
 */
public interface AiTraceQueryService {

    /**
     * 分页查询会话列表。
     *
     * @param query
     *            查询条件。
     * @return 会话摘要分页结果。
     */
    Page<AiConversationSummary> listConversations(AiTraceListQuery query);

    /**
     * 查询会话详情。
     *
     * @param id
     *            会话标识。
     * @return 会话详情；不存在时返回 null。
     */
    AiConversationDetail getConversationDetail(String id);

    /**
     * 查询概览指标。
     *
     * @param query
     *            统计参数。
     * @return 概览指标。
     */
    AiTraceOverview getOverview(AiTraceStatisticsQuery query);

    /**
     * 查询会话量趋势（按会话聚合）。
     *
     * @param query
     *            统计参数。
     * @return 趋势点列表。
     */
    List<AiTrendPoint> getConversationTrend(AiTraceStatisticsQuery query);

    /**
     * 查询意图分布（按提问聚合）。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，含占比。
     */
    List<AiCountItem> getIntentDistribution(AiTraceStatisticsQuery query);

    /**
     * 查询动作分布（按提问聚合）。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，含占比。
     */
    List<AiCountItem> getActionDistribution(AiTraceStatisticsQuery query);

    /**
     * 查询检索工具使用分布。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，含占比。
     */
    List<AiCountItem> getToolUsage(AiTraceStatisticsQuery query);

    /**
     * 查询拒答原因分布。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，含占比。
     */
    List<AiCountItem> getRefusalReasons(AiTraceStatisticsQuery query);

    /**
     * 查询统计聚合。
     *
     * @param query
     *            统计参数。
     * @return 概览、趋势与各类分布；区间内无数据时返回零值与空列表。
     */
    AiTraceStatistics getStatistics(AiTraceStatisticsQuery query);

    /**
     * 分页查询缺口提问。
     *
     * @param query
     *            查询条件。
     * @return 提问摘要分页结果。
     */
    Page<AiTurnSummary> listGapTurns(AiTraceGapQuery query);
}
