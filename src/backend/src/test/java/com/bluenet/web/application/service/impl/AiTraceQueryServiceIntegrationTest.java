package com.bluenet.web.application.service.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.application.query.aitrace.AiTraceGapQuery;
import com.bluenet.web.application.query.aitrace.AiTraceListQuery;
import com.bluenet.web.application.query.aitrace.AiTraceStatisticsQuery;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.result.aitrace.AiConversationSummary;
import com.bluenet.web.application.result.aitrace.AiCountItem;
import com.bluenet.web.application.result.aitrace.AiTraceOverview;
import com.bluenet.web.application.result.aitrace.AiTurnSummary;
import com.bluenet.web.application.service.AiTraceQueryService;
import com.bluenet.web.domain.model.enumerate.AiTraceStatisticsPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiTraceQueryService 集成测试。
 *
 * <p>
 * 覆盖会话列表分页与内嵌提问、四类筛选、会话详情、统计口径与空数据行为。 轨迹数据由测试直接写入数据库，模拟 ai-service 的采集结果。
 * </p>
 */
@DisplayName("AiTraceQueryService 集成测试")
class AiTraceQueryServiceIntegrationTest extends DBIntegrationTest {

    @Autowired
    private AiTraceQueryService aiTraceQueryService;

    @Autowired
    private JdbcTemplate jdbc;

    private static List<AiConversationSummary> contentOf(Page<AiConversationSummary> page) {
        return page.getContent();
    }

    private void insertConversation(String id, LocalDateTime createdAt, LocalDateTime lastActiveAt) {
        jdbc.update(
                "INSERT INTO tb_ai_conversation (id, created_at, last_active_at) VALUES (?, ?, ?)",
                id,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(lastActiveAt));
    }

    private void insertTurn(String conversationId, int seq, String userInput, String answer,
            String intent, String action, String extraEvents, String prompt, boolean degraded, int durationMs) {
        String events = "[{\"type\":\"intent\",\"intent\":\"" + intent + "\",\"confidence\":0.9,\"action\":\""
                + action + "\"}" + (extraEvents == null ? "" : "," + extraEvents) + "]";
        jdbc.update(
                "INSERT INTO tb_ai_turn (conversation_id, seq, user_input, answer, events, prompt, "
                        + "degraded, duration_ms, created_at) "
                        + "VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, NOW())",
                conversationId,
                seq,
                userInput,
                answer,
                events,
                prompt,
                degraded,
                durationMs);
    }

    private AiTraceListQuery listQuery() {
        return new AiTraceListQuery(null, null, null, null, null, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("listConversations: 按会话分页并内嵌提问摘要，messageCount 等于用户提问数")
    void listConversations_shouldPaginateAndEmbedTurns() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-a", now.minusMinutes(5), now.minusMinutes(1));
        insertTurn("conv-a", 1, "视觉方向要装什么软件", "答案一", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 4100);
        insertTurn("conv-a", 2, "那 CUDA 版本呢", "答案二", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 3200);
        insertTurn("conv-a", 3, "好的谢谢", "不客气", "GREETING", "DIRECT", null, null, false, 600);

        insertConversation("conv-b", now.minusMinutes(10), now.minusMinutes(8));
        insertTurn("conv-b", 1, "考核有几轮", "三轮", "ASSESSMENT_PROCESS", "RETRIEVE", null, null, false, 2300);

        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(listQuery());

        assertThat(page.getTotalElements()).isEqualTo(2);
        List<AiConversationSummary> content = contentOf(page);
        // 按最后活跃时间倒序
        assertThat(content).extracting(AiConversationSummary::getId).containsExactly("conv-a", "conv-b");

        AiConversationSummary first = content.get(0);
        assertThat(first.getMessageCount()).isEqualTo(3L);
        assertThat(first.getHiddenTurnCount()).isEqualTo(0L);
        assertThat(first.getTurns()).hasSize(3);
        assertThat(first.getTurns()).extracting(AiTurnSummary::getSeq).containsExactly(1, 2, 3);
        assertThat(first.getTurns().get(0).getUserInput()).isEqualTo("视觉方向要装什么软件");
        assertThat(first.getTurns().get(0).getIntent()).isEqualTo("SOFTWARE_DOWNLOAD");
        assertThat(first.getTurns().get(0).getAction()).isEqualTo("RETRIEVE");
        assertThat(first.getTurns().get(0).getDurationMs()).isEqualTo(4100);

        AiConversationSummary second = content.get(1);
        assertThat(second.getMessageCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listConversations: 单会话内联提问数量有上限，超出部分以 hiddenTurnCount 表示")
    void listConversations_shouldCapInlinedTurns() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-long", now.minusMinutes(5), now.minusMinutes(1));
        for (int i = 1; i <= 12; i++) {
            insertTurn("conv-long", i, "问题" + i, "答案" + i, "REGISTRATION", "RETRIEVE", null, null, false, 100);
        }

        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(listQuery());

        AiConversationSummary summary = contentOf(page).get(0);
        // 消息数仍反映真实提问总数
        assertThat(summary.getMessageCount()).isEqualTo(12L);
        // 内联上限 10 条，其余以数量表示
        assertThat(summary.getTurns()).hasSize(10);
        assertThat(summary.getHiddenTurnCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("listConversations: 未指定分页参数时默认每页 20 条")
    void listConversations_defaultPageSizeIs20() {
        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(listQuery());
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.getNumber()).isZero();
    }

    @Test
    @DisplayName("listConversations: 按意图筛选，内嵌提问只保留匹配该意图的条目")
    void listConversations_shouldFilterByIntent() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-a", now.minusMinutes(5), now.minusMinutes(1));
        insertTurn("conv-a", 1, "视觉方向要装什么软件", "答案", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 100);
        insertTurn("conv-a", 2, "好的谢谢", "不客气", "GREETING", "DIRECT", null, null, false, 100);
        insertConversation("conv-b", now.minusMinutes(5), now.minusMinutes(2));
        insertTurn("conv-b", 1, "考核有几轮", "三轮", "ASSESSMENT_PROCESS", "RETRIEVE", null, null, false, 100);

        AiTraceListQuery query = new AiTraceListQuery(
                "GREETING", null, null, null, null, PageRequest.of(0, 20));
        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(query);

        assertThat(page.getTotalElements()).isEqualTo(1);
        AiConversationSummary summary = contentOf(page).get(0);
        assertThat(summary.getId()).isEqualTo("conv-a");
        assertThat(summary.getTurns()).hasSize(1);
        assertThat(summary.getTurns().get(0).getIntent()).isEqualTo("GREETING");
    }

    @Test
    @DisplayName("listConversations: 按动作筛选")
    void listConversations_shouldFilterByAction() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-a", now.minusMinutes(5), now.minusMinutes(1));
        insertTurn("conv-a", 1, "帮我写个快排", "抱歉", "BLOCKED_CODING", "REFUSE", null, null, false, 100);
        insertConversation("conv-b", now.minusMinutes(5), now.minusMinutes(2));
        insertTurn("conv-b", 1, "考核有几轮", "三轮", "ASSESSMENT_PROCESS", "RETRIEVE", null, null, false, 100);

        AiTraceListQuery query = new AiTraceListQuery(
                null, "REFUSE", null, null, null, PageRequest.of(0, 20));
        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(query);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(contentOf(page).get(0).getId()).isEqualTo("conv-a");
    }

    @Test
    @DisplayName("listConversations: 关键词只匹配用户原始提问，不匹配事件内容")
    void listConversations_keywordShouldOnlyMatchUserInput() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-a", now.minusMinutes(5), now.minusMinutes(1));
        // 事件里含有「独有片段」，但用户提问里没有
        String extra = "{\"type\":\"tool_result\",\"tool_name\":\"chunk_search\",\"content\":\"独有片段\"}";
        insertTurn("conv-a", 1, "普通问题文本", "答案", "REGISTRATION", "RETRIEVE", extra, null, false, 100);
        insertConversation("conv-b", now.minusMinutes(5), now.minusMinutes(2));
        insertTurn("conv-b", 1, "含关键词的提问", "答案", "REGISTRATION", "RETRIEVE", null, null, false, 100);

        Page<AiConversationSummary> byUserInput = aiTraceQueryService.listConversations(
                new AiTraceListQuery(
                        null, null, "含关键词", null, null, PageRequest.of(0, 20)));
        assertThat(byUserInput.getTotalElements()).isEqualTo(1);
        assertThat(contentOf(byUserInput).get(0).getId()).isEqualTo("conv-b");

        Page<AiConversationSummary> byEventContent = aiTraceQueryService.listConversations(
                new AiTraceListQuery(
                        null, null, "独有片段", null, null, PageRequest.of(0, 20)));
        assertThat(byEventContent.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("listConversations: 按会话最后活跃时间范围筛选")
    void listConversations_shouldFilterByTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-old", now.minusDays(10), now.minusDays(10));
        insertTurn("conv-old", 1, "很久以前", "答案", "REGISTRATION", "RETRIEVE", null, null, false, 100);
        insertConversation("conv-new", now.minusHours(1), now.minusHours(1));
        insertTurn("conv-new", 1, "刚刚", "答案", "REGISTRATION", "RETRIEVE", null, null, false, 100);

        Page<AiConversationSummary> page = aiTraceQueryService.listConversations(
                new AiTraceListQuery(
                        null, null, null, now.minusDays(7), now, PageRequest.of(0, 20)));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(contentOf(page).get(0).getId()).isEqualTo("conv-new");
    }

    @Test
    @DisplayName("getConversationDetail: 返回完整原始事件序列与 prompt 快照")
    void getConversationDetail_shouldReturnRawEventsAndPrompt() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-d", now.minusMinutes(5), now.minusMinutes(1));
        String toolEvent = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search_by_tags\","
                + "\"tool_args\":{\"query\":\"CUDA\"},\"round\":1}";
        insertTurn(
                "conv-d",
                1,
                "原始提问",
                "最终答案",
                "SOFTWARE_DOWNLOAD",
                "RETRIEVE",
                toolEvent,
                "[{\"role\":\"system\",\"content\":\"sys\"}]",
                false,
                4200);
        insertTurn("conv-d", 2, "第二轮", "答案二", "GREETING", "DIRECT", null, null, true, 300);

        AiConversationDetail detail = aiTraceQueryService.getConversationDetail("conv-d");

        assertThat(detail).isNotNull();
        assertThat(detail.getMessageCount()).isEqualTo(2L);
        assertThat(detail.getTurns()).hasSize(2);

        // 事件序列原样返回，含工具调用参数
        assertThat(detail.getTurns().get(0).getEvents()).contains("chunk_search_by_tags").contains("CUDA");
        assertThat(detail.getTurns().get(0).getToolRounds()).isEqualTo(1);
        assertThat(detail.getTurns().get(0).getPrompt()).contains("role").contains("system");
        // 用户原话与富化内容分离
        assertThat(detail.getTurns().get(0).getUserInput()).isEqualTo("原始提问");

        // 非检索路径无 prompt 快照，且 degraded 被保留
        assertThat(detail.getTurns().get(1).getPrompt()).isNull();
        assertThat(detail.getTurns().get(1).getDegraded()).isTrue();
    }

    @Test
    @DisplayName("getConversationDetail: 会话不存在时返回 null 而不是抛异常")
    void getConversationDetail_shouldReturnNullForUnknownId() {
        assertThat(aiTraceQueryService.getConversationDetail("does-not-exist")).isNull();
    }

    @Test
    @DisplayName("统计接口: 无数据时返回空列表与零值，不报错")
    void statistics_shouldReturnEmptyAndZeroWhenNoData() {
        AiTraceStatisticsQuery query = new AiTraceStatisticsQuery(AiTraceStatisticsPeriod.D7);

        assertThat(aiTraceQueryService.getIntentDistribution(query)).isEmpty();
        assertThat(aiTraceQueryService.getActionDistribution(query)).isEmpty();
        assertThat(aiTraceQueryService.getToolUsage(query)).isEmpty();
        assertThat(aiTraceQueryService.getRefusalReasons(query)).isEmpty();

        AiTraceOverview overview = aiTraceQueryService.getOverview(query);
        assertThat(overview.getConversationCount()).isZero();
        assertThat(overview.getQuestionCount()).isZero();
        assertThat(overview.getFallbackRate()).isZero();

        // 趋势仍返回完整时间桶（7 天 7 个点），全部为 0
        assertThat(aiTraceQueryService.getConversationTrend(query)).hasSize(7);
    }

    @Test
    @DisplayName("统计接口: 意图与动作分布按提问聚合，占比之和为 1")
    void statistics_shouldAggregateDistributionsByQuestion() {
        LocalDateTime now = LocalDateTime.now();
        // 一个会话内 3 条提问，其中 2 条检索、1 条拒答
        insertConversation("conv-s1", now.minusMinutes(5), now.minusMinutes(1));
        insertTurn("conv-s1", 1, "问题1", "答案", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 100);
        insertTurn("conv-s1", 2, "问题2", "答案", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 100);
        insertTurn("conv-s1", 3, "问题3", "抱歉", "BLOCKED_CODING", "REFUSE", null, null, false, 100);
        // 另一个会话 1 条提问
        insertConversation("conv-s2", now.minusMinutes(9), now.minusMinutes(9));
        insertTurn("conv-s2", 1, "问题4", "你好", "GREETING", "DIRECT", null, null, false, 100);

        AiTraceStatisticsQuery query = new AiTraceStatisticsQuery(AiTraceStatisticsPeriod.D7);

        // 意图分布：按提问统计（SOFTWARE_DOWNLOAD 2 条）
        List<AiCountItem> intents = aiTraceQueryService.getIntentDistribution(query);
        assertThat(intents).extracting(AiCountItem::getLabel)
                .contains("SOFTWARE_DOWNLOAD", "BLOCKED_CODING", "GREETING");
        AiCountItem software = intents.stream()
                .filter(i -> "SOFTWARE_DOWNLOAD".equals(i.getLabel()))
                .findFirst()
                .orElseThrow();
        assertThat(software.getCount()).isEqualTo(2L);
        assertThat(software.getRatio()).isEqualTo(2.0d / 4.0d);

        // 动作分布：检索 2、拒答 1、直接回复 1
        List<AiCountItem> actions = aiTraceQueryService.getActionDistribution(query);
        assertThat(actions).extracting(AiCountItem::getLabel).containsExactlyInAnyOrder("RETRIEVE", "REFUSE", "DIRECT");
        assertThat(actions.stream().filter(a -> "RETRIEVE".equals(a.getLabel())).findFirst().orElseThrow().getCount())
                .isEqualTo(2L);
        assertThat(actions.stream().mapToDouble(AiCountItem::getRatio).sum())
                .isEqualTo(1.0d, org.assertj.core.data.Offset.offset(1e-9));

        // 拒答原因分布：只含被拦截的意图
        List<AiCountItem> refusals = aiTraceQueryService.getRefusalReasons(query);
        assertThat(refusals).hasSize(1);
        assertThat(refusals.get(0).getLabel()).isEqualTo("BLOCKED_CODING");
        assertThat(refusals.get(0).getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("统计接口: 工具使用分布按调用次数降序，兜底检索可被识别")
    void statistics_shouldReportToolUsageIncludingFallback() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-t", now.minusMinutes(5), now.minusMinutes(1));
        String byTags = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search_by_tags\",\"tool_args\":{},\"round\":1}";
        String byTags2 = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search_by_tags\",\"tool_args\":{},\"round\":2}";
        String fallback = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search\",\"tool_args\":{},\"round\":3}";
        insertTurn(
                "conv-t",
                1,
                "问题",
                "答案",
                "SOFTWARE_DOWNLOAD",
                "RETRIEVE",
                byTags + "," + byTags2 + "," + fallback,
                null,
                false,
                100);

        List<AiCountItem> tools = aiTraceQueryService.getToolUsage(
                new AiTraceStatisticsQuery(AiTraceStatisticsPeriod.D7));

        assertThat(tools).extracting(AiCountItem::getLabel).containsExactly("chunk_search_by_tags", "chunk_search");
        assertThat(tools.get(0).getCount()).isEqualTo(2L);
        assertThat(tools.get(1).getLabel()).isEqualTo("chunk_search");
    }

    @Test
    @DisplayName("统计接口: 概览的兜底检索率以提问数为分母")
    void statistics_shouldComputeFallbackRate() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-o", now.minusMinutes(5), now.minusMinutes(1));
        String fallback = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search\",\"tool_args\":{},\"round\":1}";
        // 4 条提问，其中 1 条触发兜底
        insertTurn("conv-o", 1, "q1", "a", "SOFTWARE_DOWNLOAD", "RETRIEVE", fallback, null, false, 100);
        insertTurn("conv-o", 2, "q2", "a", "SOFTWARE_DOWNLOAD", "RETRIEVE", null, null, false, 100);
        insertTurn("conv-o", 3, "q3", "a", "GREETING", "DIRECT", null, null, false, 100);
        insertTurn("conv-o", 4, "q4", "a", "BLOCKED_CODING", "REFUSE", null, null, false, 100);

        AiTraceOverview overview = aiTraceQueryService.getOverview(
                new AiTraceStatisticsQuery(AiTraceStatisticsPeriod.D7));

        assertThat(overview.getConversationCount()).isEqualTo(1L);
        assertThat(overview.getQuestionCount()).isEqualTo(4L);
        assertThat(overview.getRefusedCount()).isEqualTo(1L);
        assertThat(overview.getFallbackCount()).isEqualTo(1L);
        assertThat(overview.getFallbackRate()).isEqualTo(0.25d);
    }

    @Test
    @DisplayName("统计接口: 趋势按会话聚合，返回完整时间桶")
    void statistics_shouldAggregateTrendByConversation() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-tr1", now.minusHours(2), now.minusHours(1));
        insertTurn("conv-tr1", 1, "q1", "a", "GREETING", "DIRECT", null, null, false, 100);
        insertTurn("conv-tr1", 2, "q2", "a", "GREETING", "DIRECT", null, null, false, 100);
        insertConversation("conv-tr2", now.minusHours(3), now.minusHours(3));

        var trend = aiTraceQueryService.getConversationTrend(new AiTraceStatisticsQuery(AiTraceStatisticsPeriod.D7));

        // 7 天按天补零，共 7 个桶
        assertThat(trend).hasSize(7);
        // 两个会话都在今天 -> 会话维度计数为 2（而不是 3 条提问）
        assertThat(trend.stream().mapToLong(p -> p.getCount()).sum()).isEqualTo(2L);
    }

    @Test
    @DisplayName("缺口查询: 按类型筛选未听懂 / 被拒答 / 标签未覆盖")
    void listGapTurns_shouldFilterByGapType() {
        LocalDateTime now = LocalDateTime.now();
        insertConversation("conv-g", now.minusMinutes(5), now.minusMinutes(1));
        // CLARIFY：直接回复且意图为 CLARIFY
        insertTurn("conv-g", 1, "那个东西怎么弄", "抱歉", "CLARIFY", "DIRECT", null, null, false, 100);
        // REFUSE
        insertTurn("conv-g", 2, "帮我写个快排", "抱歉", "BLOCKED_CODING", "REFUSE", null, null, false, 100);
        // FALLBACK：触发兜底语义检索
        String fallback = "{\"type\":\"tool_call\",\"tool_name\":\"chunk_search\",\"tool_args\":{},\"round\":1}";
        insertTurn("conv-g", 3, "报销流程", "答案", "REGISTRATION", "RETRIEVE", fallback, null, false, 100);
        // 正常提问，不应出现在任何缺口里
        insertTurn("conv-g", 4, "考核有几轮", "三轮", "ASSESSMENT_PROCESS", "RETRIEVE", null, null, false, 100);

        Page<AiTurnSummary> clarify = aiTraceQueryService.listGapTurns(
                new AiTraceGapQuery("CLARIFY", PageRequest.of(0, 20)));
        assertThat(clarify.getContent()).extracting(AiTurnSummary::getUserInput).containsExactly("那个东西怎么弄");

        Page<AiTurnSummary> refuse = aiTraceQueryService.listGapTurns(
                new AiTraceGapQuery("REFUSE", PageRequest.of(0, 20)));
        assertThat(refuse.getContent()).extracting(AiTurnSummary::getUserInput).containsExactly("帮我写个快排");

        Page<AiTurnSummary> fallbackGap = aiTraceQueryService.listGapTurns(
                new AiTraceGapQuery("FALLBACK", PageRequest.of(0, 20)));
        assertThat(fallbackGap.getContent()).extracting(AiTurnSummary::getUserInput).containsExactly("报销流程");

        // 未知类型不返回任何数据，而不是返回全部
        Page<AiTurnSummary> unknown = aiTraceQueryService.listGapTurns(
                new AiTraceGapQuery("UNKNOWN", PageRequest.of(0, 20)));
        assertThat(unknown.getContent()).isEmpty();
    }
}
