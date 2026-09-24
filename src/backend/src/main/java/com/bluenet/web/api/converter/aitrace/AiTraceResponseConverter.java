package com.bluenet.web.api.converter.aitrace;

import io.github.ivencn.infra.web.response.PageDTO;
import com.bluenet.web.api.dto.aitrace.AiConversationDetailDTO;
import com.bluenet.web.api.dto.aitrace.AiConversationSummaryDTO;
import com.bluenet.web.api.dto.aitrace.AiCountItemDTO;
import com.bluenet.web.api.dto.aitrace.AiTraceOverviewDTO;
import com.bluenet.web.api.dto.aitrace.AiTraceStatisticsDTO;
import com.bluenet.web.api.dto.aitrace.AiTrendPointDTO;
import com.bluenet.web.api.dto.aitrace.AiTurnDetailDTO;
import com.bluenet.web.api.dto.aitrace.AiTurnSummaryDTO;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.result.aitrace.AiConversationSummary;
import com.bluenet.web.application.result.aitrace.AiCountItem;
import com.bluenet.web.application.result.aitrace.AiTraceOverview;
import com.bluenet.web.application.result.aitrace.AiTraceStatistics;
import com.bluenet.web.application.result.aitrace.AiTrendPoint;
import com.bluenet.web.application.result.aitrace.AiTurnDetail;
import com.bluenet.web.application.result.aitrace.AiTurnSummary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 对话轨迹响应转换器。
 *
 * <p>
 * 负责把应用层结果对象转换为接口 DTO。
 * </p>
 */
@Component
public class AiTraceResponseConverter {

    public PageDTO<AiConversationSummaryDTO> toConversationPageDTO(Page<AiConversationSummary> page) {
        return PageDTO.from(page.map(this::toConversationDTO));
    }

    public PageDTO<AiTurnSummaryDTO> toTurnSummaryPageDTO(Page<AiTurnSummary> page) {
        return PageDTO.from(page.map(this::toTurnSummaryDTO));
    }

    public AiConversationSummaryDTO toConversationDTO(AiConversationSummary result) {
        AiConversationSummaryDTO dto = new AiConversationSummaryDTO();
        dto.setId(result.getId());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setLastActiveAt(result.getLastActiveAt());
        dto.setMessageCount(result.getMessageCount());
        dto.setHiddenTurnCount(result.getHiddenTurnCount());
        dto.setTurns(
                result.getTurns() == null
                        ? List.of()
                        : result.getTurns().stream().map(this::toTurnSummaryDTO).toList());
        return dto;
    }

    public AiTurnSummaryDTO toTurnSummaryDTO(AiTurnSummary result) {
        AiTurnSummaryDTO dto = new AiTurnSummaryDTO();
        dto.setSeq(result.getSeq());
        dto.setUserInput(result.getUserInput());
        dto.setIntent(result.getIntent());
        dto.setAction(result.getAction());
        dto.setToolRounds(result.getToolRounds());
        dto.setDurationMs(result.getDurationMs());
        dto.setDegraded(result.getDegraded());
        dto.setCreatedAt(result.getCreatedAt());
        return dto;
    }

    public AiConversationDetailDTO toDetailDTO(AiConversationDetail result) {
        AiConversationDetailDTO dto = new AiConversationDetailDTO();
        dto.setId(result.getId());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setLastActiveAt(result.getLastActiveAt());
        dto.setMessageCount(result.getMessageCount());
        dto.setTurns(
                result.getTurns() == null
                        ? List.of()
                        : result.getTurns().stream().map(this::toTurnDetailDTO).toList());
        return dto;
    }

    public AiTurnDetailDTO toTurnDetailDTO(AiTurnDetail result) {
        AiTurnDetailDTO dto = new AiTurnDetailDTO();
        dto.setSeq(result.getSeq());
        dto.setUserInput(result.getUserInput());
        dto.setAnswer(result.getAnswer());
        dto.setIntent(result.getIntent());
        dto.setAction(result.getAction());
        dto.setToolRounds(result.getToolRounds());
        dto.setDurationMs(result.getDurationMs());
        dto.setDegraded(result.getDegraded());
        dto.setCreatedAt(result.getCreatedAt());
        dto.setEvents(result.getEvents());
        dto.setPrompt(result.getPrompt());
        return dto;
    }

    public AiTraceOverviewDTO toOverviewDTO(AiTraceOverview result) {
        AiTraceOverviewDTO dto = new AiTraceOverviewDTO();
        dto.setConversationCount(result.getConversationCount());
        dto.setQuestionCount(result.getQuestionCount());
        dto.setRefusedCount(result.getRefusedCount());
        dto.setFallbackCount(result.getFallbackCount());
        dto.setFallbackRate(result.getFallbackRate());
        return dto;
    }

    public AiTrendPointDTO toTrendPointDTO(AiTrendPoint result) {
        AiTrendPointDTO dto = new AiTrendPointDTO();
        dto.setTime(result.getTime());
        dto.setCount(result.getCount());
        return dto;
    }

    public List<AiTrendPointDTO> toTrendPointDTOList(List<AiTrendPoint> results) {
        return results.stream().map(this::toTrendPointDTO).toList();
    }

    public AiCountItemDTO toCountItemDTO(AiCountItem result) {
        AiCountItemDTO dto = new AiCountItemDTO();
        dto.setLabel(result.getLabel());
        dto.setCount(result.getCount());
        dto.setRatio(result.getRatio());
        return dto;
    }

    public List<AiCountItemDTO> toCountItemDTOList(List<AiCountItem> results) {
        return results.stream().map(this::toCountItemDTO).toList();
    }
    public AiTraceStatisticsDTO toStatisticsDTO(AiTraceStatistics result) {
        AiTraceStatisticsDTO dto = new AiTraceStatisticsDTO();
        dto.setOverview(toOverviewDTO(result.getOverview()));
        dto.setTrend(toTrendPointDTOList(result.getTrend()));
        dto.setIntents(toCountItemDTOList(result.getIntents()));
        dto.setActions(toCountItemDTOList(result.getActions()));
        dto.setTools(toCountItemDTOList(result.getTools()));
        dto.setRefusals(toCountItemDTOList(result.getRefusals()));
        return dto;
    }
}
