package com.bluenet.web.domain.repository;

import com.bluenet.web.application.query.aitrace.AiTraceGapQuery;
import com.bluenet.web.application.query.aitrace.AiTraceListQuery;
import com.bluenet.web.application.query.aitrace.AiTraceStatisticsQuery;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.result.aitrace.AiConversationSummary;
import com.bluenet.web.application.result.aitrace.AiCountItem;
import com.bluenet.web.application.result.aitrace.AiTraceOverview;
import com.bluenet.web.application.result.aitrace.AiTrendPoint;
import com.bluenet.web.application.result.aitrace.AiTurnSummary;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * AI 对话轨迹仓储接口。
 *
 * <p>
 * 只读仓储：轨迹由 ai-service 写入，本服务仅查询。 查询对象直接复用应用层入参，避免为纯读取场景再包一层等价参数对象。
 * </p>
 */
public interface AiTraceRepository {

    /**
     * 分页查询会话列表，并内嵌其提问摘要。
     *
     * @param query
     *            查询条件。
     * @return 会话摘要分页结果。
     */
    Page<AiConversationSummary> findConversations(AiTraceListQuery query);

    /**
     * 查询单个会话的完整详情。
     *
     * @param id
     *            会话标识。
     * @return 会话详情；不存在时返回空。
     */
    Optional<AiConversationDetail> findConversation(String id);

    /**
     * 查询意图分布（按提问聚合）。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，占比由应用层计算。
     */
    List<AiCountItem> queryIntentDistribution(AiTraceStatisticsQuery query);

    /**
     * 查询动作分布（按提问聚合）。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表。
     */
    List<AiCountItem> queryActionDistribution(AiTraceStatisticsQuery query);

    /**
     * 查询检索工具使用分布。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，按调用次数降序。
     */
    List<AiCountItem> queryToolUsage(AiTraceStatisticsQuery query);

    /**
     * 查询拒答原因分布。
     *
     * @param query
     *            统计参数。
     * @return 计数项列表，按次数降序。
     */
    List<AiCountItem> queryRefusalReasons(AiTraceStatisticsQuery query);

    /**
     * 查询会话量趋势（按会话聚合）。
     *
     * @param query
     *            统计参数。
     * @return 趋势点列表，无数据的时间桶返回 0。
     */
    List<AiTrendPoint> queryConversationTrend(AiTraceStatisticsQuery query);

    /**
     * 查询概览指标。
     *
     * @param query
     *            统计参数。
     * @return 概览指标；区间内无数据时各项为 0。
     */
    AiTraceOverview queryOverview(AiTraceStatisticsQuery query);

    /**
     * 分页查询缺口提问。
     *
     * @param query
     *            查询条件。
     * @return 提问摘要分页结果。
     */
    Page<AiTurnSummary> findGapTurns(AiTraceGapQuery query);
}
