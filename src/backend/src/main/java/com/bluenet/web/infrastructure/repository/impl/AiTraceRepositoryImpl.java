package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.application.query.aitrace.AiTraceGapQuery;
import com.bluenet.web.application.query.aitrace.AiTraceListQuery;
import com.bluenet.web.application.query.aitrace.AiTraceStatisticsQuery;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.result.aitrace.AiConversationSummary;
import com.bluenet.web.application.result.aitrace.AiCountItem;
import com.bluenet.web.application.result.aitrace.AiTraceOverview;
import com.bluenet.web.application.result.aitrace.AiTrendPoint;
import com.bluenet.web.application.result.aitrace.AiTurnDetail;
import com.bluenet.web.application.result.aitrace.AiTurnSummary;
import com.bluenet.web.domain.model.enumerate.AiTraceStatisticsPeriod;
import com.bluenet.web.domain.repository.AiTraceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiConversationRowQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiCountQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiOverviewQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTrendPointQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTurnDetailQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AiTurnSummaryQueryDO;
import com.bluenet.web.infrastructure.repository.mapper.AiTraceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI 对话轨迹仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class AiTraceRepositoryImpl implements AiTraceRepository {

    /**
     * 会话列表内联的提问上限：超出部分只返回数量，避免单个长会话撑爆列表响应。
     */
    private static final int INLINE_TURN_LIMIT = 10;

    private final AiTraceMapper aiTraceMapper;

    @Override
    public org.springframework.data.domain.Page<AiConversationSummary> findConversations(AiTraceListQuery query) {
        Pageable pageable = query.pageable();
        IPage<AiConversationRowQueryDO> rowPage = aiTraceMapper.selectConversations(
                new Page<>(pageable.getPageNumber() + 1L, pageable.getPageSize()),
                query.intent(),
                query.action(),
                query.keyword(),
                query.startTime(),
                query.endTime());

        List<String> ids = rowPage.getRecords().stream().map(AiConversationRowQueryDO::getId).toList();
        Map<String, List<AiTurnSummary>> turnsByConversation = loadTurnSummaries(
                ids,
                query.intent(),
                query.action());

        List<AiConversationSummary> summaries = new ArrayList<>(rowPage.getRecords().size());
        for (AiConversationRowQueryDO row : rowPage.getRecords()) {
            summaries.add(toSummary(row, turnsByConversation.getOrDefault(row.getId(), List.of())));
        }
        return new PageImpl<>(summaries, pageable, rowPage.getTotal());
    }

    @Override
    public Optional<AiConversationDetail> findConversation(String id) {
        AiConversationRowQueryDO row = aiTraceMapper.selectConversation(id);
        if (row == null) {
            return Optional.empty();
        }
        List<AiTurnDetail> turns = aiTraceMapper.selectTurnDetails(id)
                .stream()
                .map(this::toTurnDetail)
                .toList();
        return Optional.of(
                AiConversationDetail.builder()
                        .id(row.getId())
                        .createdAt(row.getCreatedAt())
                        .lastActiveAt(row.getLastActiveAt())
                        .messageCount(row.getMessageCount())
                        .turns(turns)
                        .build());
    }

    @Override
    public List<AiCountItem> queryIntentDistribution(AiTraceStatisticsQuery query) {
        return toCountItems(aiTraceMapper.selectIntentDistribution(fromOf(query.period())));
    }

    @Override
    public List<AiCountItem> queryActionDistribution(AiTraceStatisticsQuery query) {
        return toCountItems(aiTraceMapper.selectActionDistribution(fromOf(query.period())));
    }

    @Override
    public List<AiCountItem> queryToolUsage(AiTraceStatisticsQuery query) {
        return toCountItems(aiTraceMapper.selectToolUsage(fromOf(query.period())));
    }

    @Override
    public List<AiCountItem> queryRefusalReasons(AiTraceStatisticsQuery query) {
        return toCountItems(aiTraceMapper.selectRefusalReasons(fromOf(query.period())));
    }

    @Override
    public List<AiTrendPoint> queryConversationTrend(AiTraceStatisticsQuery query) {
        return aiTraceMapper.selectConversationTrend(query.period().name())
                .stream()
                .map(this::toTrendPoint)
                .toList();
    }

    @Override
    public AiTraceOverview queryOverview(AiTraceStatisticsQuery query) {
        AiOverviewQueryDO row = aiTraceMapper.selectOverview(fromOf(query.period()));
        long conversations = valueOf(row == null ? null : row.getConversationCount());
        long questions = valueOf(row == null ? null : row.getQuestionCount());
        long refused = valueOf(row == null ? null : row.getRefusedCount());
        long fallback = valueOf(row == null ? null : row.getFallbackCount());
        return AiTraceOverview.builder()
                .conversationCount(conversations)
                .questionCount(questions)
                .refusedCount(refused)
                .fallbackCount(fallback)
                .fallbackRate(questions == 0 ? 0.0d : (double) fallback / questions)
                .build();
    }

    @Override
    public org.springframework.data.domain.Page<AiTurnSummary> findGapTurns(AiTraceGapQuery query) {
        Pageable pageable = query.pageable();
        IPage<AiTurnSummaryQueryDO> rowPage = aiTraceMapper.selectGapTurns(
                new Page<>(pageable.getPageNumber() + 1L, pageable.getPageSize()),
                query.gapType());
        List<AiTurnSummary> items = rowPage.getRecords().stream().map(this::toTurnSummary).toList();
        return new PageImpl<>(items, pageable, rowPage.getTotal());
    }

    // ------------------------------------------------------------------
    // 内部转换
    // ------------------------------------------------------------------

    /**
     * 批量加载会话内嵌的提问摘要，并按会话分组。
     */
    private Map<String, List<AiTurnSummary>> loadTurnSummaries(List<String> conversationIds,
            String intent, String action) {
        if (conversationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<AiTurnSummary>> grouped = new LinkedHashMap<>();
        for (AiTurnSummaryQueryDO row : aiTraceMapper.selectTurnSummaries(conversationIds, intent, action)) {
            grouped.computeIfAbsent(row.getConversationId(), key -> new ArrayList<>()).add(toTurnSummary(row));
        }
        return grouped;
    }

    private AiConversationSummary toSummary(AiConversationRowQueryDO row, List<AiTurnSummary> turns) {
        List<AiTurnSummary> inlined = turns.size() > INLINE_TURN_LIMIT
                ? turns.subList(0, INLINE_TURN_LIMIT)
                : turns;
        return AiConversationSummary.builder()
                .id(row.getId())
                .createdAt(row.getCreatedAt())
                .lastActiveAt(row.getLastActiveAt())
                .messageCount(row.getMessageCount())
                .turns(new ArrayList<>(inlined))
                .hiddenTurnCount(Math.max(0L, turns.size() - (long) inlined.size()))
                .build();
    }

    private AiTurnSummary toTurnSummary(AiTurnSummaryQueryDO row) {
        return AiTurnSummary.builder()
                .seq(row.getSeq())
                .userInput(row.getUserInput())
                .intent(row.getIntent())
                .action(row.getAction())
                .toolRounds(row.getToolRounds())
                .durationMs(row.getDurationMs())
                .degraded(row.getDegraded())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AiTurnDetail toTurnDetail(AiTurnDetailQueryDO row) {
        return AiTurnDetail.builder()
                .seq(row.getSeq())
                .userInput(row.getUserInput())
                .answer(row.getAnswer())
                .intent(row.getIntent())
                .action(row.getAction())
                .toolRounds(row.getToolRounds())
                .durationMs(row.getDurationMs())
                .degraded(row.getDegraded())
                .createdAt(row.getCreatedAt())
                .events(row.getEvents())
                .prompt(row.getPrompt())
                .build();
    }

    private AiTrendPoint toTrendPoint(AiTrendPointQueryDO row) {
        return AiTrendPoint.builder().time(row.getTime()).count(row.getCount()).build();
    }

    private List<AiCountItem> toCountItems(List<AiCountQueryDO> rows) {
        return rows.stream()
                .map(row -> AiCountItem.builder().label(row.getLabel()).count(row.getCount()).build())
                .toList();
    }

    private long valueOf(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 把统计周期换算为统计窗口起点。
     */
    private LocalDateTime fromOf(AiTraceStatisticsPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case H24 -> now.minusHours(24);
            case D7 -> now.minusDays(7);
            case D30 -> now.minusDays(30);
        };
    }
}
