package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiConversationRowQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiCountQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiOverviewQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTrendPointQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTurnDetailQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTurnSummaryQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 对话轨迹 Mapper。
 *
 * <p>
 * 全部方法为只读投影查询：围绕 events JSONB 做提取与聚合，返回 {@code dataobject/query}
 * 下的投影对象，由仓储实现负责转换为应用层结果对象。 之所以在 Mapper 层直接投影（而非返回完整 DO 后由应用层筛选聚合），
 * 是因为意图/动作/轮次等字段只存在于 events JSONB 内，必须在 SQL 层提取。
 * </p>
 */
@Mapper
public interface AiTraceMapper {

    /**
     * 分页查询会话列表。
     *
     * @param page
     *            分页参数（由 MyBatis-Plus 分页插件处理）。
     * @param intent
     *            意图筛选，为空表示不限。
     * @param action
     *            动作筛选，为空表示不限。
     * @param keyword
     *            关键词，仅匹配用户原始提问文本。
     * @param startTime
     *            会话最后活跃时间下界，可为空。
     * @param endTime
     *            会话最后活跃时间上界，可为空。
     * @return 会话投影分页结果。
     */
    IPage<AiConversationRowQueryDO> selectConversations(IPage<AiConversationRowQueryDO> page,
            @Param("intent") String intent,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定会话的会话投影行。
     *
     * @param id
     *            会话标识。
     * @return 会话投影，不存在时返回 null。
     */
    AiConversationRowQueryDO selectConversation(@Param("id") String id);

    /**
     * 批量查询会话内嵌的提问摘要。
     *
     * @param conversationIds
     *            会话标识集合。
     * @param intent
     *            意图筛选，为空表示不限；指定时只返回匹配该意图的提问。
     * @param action
     *            动作筛选，为空表示不限。
     * @return 按会话与序号排序的提问摘要投影。
     */
    List<AiTurnSummaryQueryDO> selectTurnSummaries(@Param("conversationIds") List<String> conversationIds,
            @Param("intent") String intent,
            @Param("action") String action);

    /**
     * 查询指定会话的全部提问详情，按序号升序。
     *
     * @param conversationId
     *            会话标识。
     * @return 提问详情投影列表。
     */
    List<AiTurnDetailQueryDO> selectTurnDetails(@Param("conversationId") String conversationId);

    /**
     * 意图分布（按提问聚合）。
     *
     * @param from
     *            统计起始时间。
     * @return 按次数降序的计数投影。
     */
    List<AiCountQueryDO> selectIntentDistribution(@Param("from") LocalDateTime from);

    /**
     * 动作分布（按提问聚合）。
     *
     * @param from
     *            统计起始时间。
     * @return 按次数降序的计数投影。
     */
    List<AiCountQueryDO> selectActionDistribution(@Param("from") LocalDateTime from);

    /**
     * 检索工具使用分布（按工具调用事件聚合）。
     *
     * @param from
     *            统计起始时间。
     * @return 按调用次数降序的计数投影。
     */
    List<AiCountQueryDO> selectToolUsage(@Param("from") LocalDateTime from);

    /**
     * 拒答原因分布（按被拦截的意图聚合）。
     *
     * @param from
     *            统计起始时间。
     * @return 按次数降序的计数投影。
     */
    List<AiCountQueryDO> selectRefusalReasons(@Param("from") LocalDateTime from);

    /**
     * 会话量趋势（按会话聚合，时间桶补零）。
     *
     * @param period
     *            统计周期枚举名：H24 / D7 / D30。
     * @return 趋势点投影列表。
     */
    List<AiTrendPointQueryDO> selectConversationTrend(@Param("period") String period);

    /**
     * 概览指标。
     *
     * @param from
     *            统计起始时间。
     * @return 概览投影。
     */
    AiOverviewQueryDO selectOverview(@Param("from") LocalDateTime from);

    /**
     * 分页查询缺口提问。
     *
     * @param page
     *            分页参数（由 MyBatis-Plus 分页插件处理）。
     * @param gapType
     *            缺口类型：CLARIFY / REFUSE / FALLBACK。
     * @return 提问摘要投影分页结果。
     */
    IPage<AiTurnSummaryQueryDO> selectGapTurns(IPage<AiTurnSummaryQueryDO> page,
            @Param("gapType") String gapType);
}
