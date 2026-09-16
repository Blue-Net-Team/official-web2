package com.bluenet.web.application.service.impl;

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
import com.bluenet.web.application.service.AiTraceQueryService;
import com.bluenet.web.domain.repository.AiTraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 对话轨迹查询应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class AiTraceQueryServiceImpl implements AiTraceQueryService {

    private final AiTraceRepository aiTraceRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AiConversationSummary> listConversations(AiTraceListQuery query) {
        return aiTraceRepository.findConversations(query);
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversationDetail getConversationDetail(String id) {
        return aiTraceRepository.findConversation(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AiTraceOverview getOverview(AiTraceStatisticsQuery query) {
        return aiTraceRepository.queryOverview(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiTrendPoint> getConversationTrend(AiTraceStatisticsQuery query) {
        return aiTraceRepository.queryConversationTrend(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCountItem> getIntentDistribution(AiTraceStatisticsQuery query) {
        return withRatio(aiTraceRepository.queryIntentDistribution(query));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCountItem> getActionDistribution(AiTraceStatisticsQuery query) {
        return withRatio(aiTraceRepository.queryActionDistribution(query));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCountItem> getToolUsage(AiTraceStatisticsQuery query) {
        return withRatio(aiTraceRepository.queryToolUsage(query));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCountItem> getRefusalReasons(AiTraceStatisticsQuery query) {
        return withRatio(aiTraceRepository.queryRefusalReasons(query));
    }

    @Override
    @Transactional(readOnly = true)
    public AiTraceStatistics getStatistics(AiTraceStatisticsQuery query) {
        return AiTraceStatistics.builder()
                .overview(getOverview(query))
                .trend(getConversationTrend(query))
                .intents(getIntentDistribution(query))
                .actions(getActionDistribution(query))
                .tools(getToolUsage(query))
                .refusals(getRefusalReasons(query))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiTurnSummary> listGapTurns(AiTraceGapQuery query) {
        return aiTraceRepository.findGapTurns(query);
    }

    /**
     * 为分布类计数项补充占比。占比以同一次查询返回的总数为分母。
     *
     * @param items
     *            原始计数项。
     * @return 带占比的计数项；总数为 0 时占比为 0。
     */
    private List<AiCountItem> withRatio(List<AiCountItem> items) {
        long total = items.stream().mapToLong(item -> item.getCount() == null ? 0L : item.getCount()).sum();
        return items.stream().map(item -> {
            long count = item.getCount() == null ? 0L : item.getCount();
            return AiCountItem.builder()
                    .label(item.getLabel())
                    .count(item.getCount())
                    .ratio(total == 0L ? 0.0d : (double) count / total)
                    .build();
        }).toList();
    }
}
