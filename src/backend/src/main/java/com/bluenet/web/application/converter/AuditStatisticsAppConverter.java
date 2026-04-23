package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.application.AuditStatisticsResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 审计统计应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AuditStatisticsAppConverter {

    public TrendPointDTO toDTO(AuditStatisticsResult.TrendPoint result) {
        TrendPointDTO dto = new TrendPointDTO();
        dto.setTime(result.time());
        dto.setCount(result.count());
        return dto;
    }

    public List<TrendPointDTO> toTrendPointDTOList(List<AuditStatisticsResult.TrendPoint> results) {
        return results.stream().map(this::toDTO).toList();
    }

    public EndpointRankingDTO toDTO(AuditStatisticsResult.EndpointRanking result) {
        EndpointRankingDTO dto = new EndpointRankingDTO();
        dto.setPattern(result.pattern());
        dto.setCount(result.count());
        dto.setAvgDurationMs(result.avgDurationMs());
        dto.setErrorCount(result.errorCount());
        return dto;
    }

    public List<EndpointRankingDTO> toEndpointRankingDTOList(List<AuditStatisticsResult.EndpointRanking> results) {
        return results.stream().map(this::toDTO).toList();
    }

    public EndpointLatencyDTO toDTO(AuditStatisticsResult.EndpointLatency result) {
        EndpointLatencyDTO dto = new EndpointLatencyDTO();
        dto.setPattern(result.pattern());
        dto.setAvgDurationMs(result.avgDurationMs());
        dto.setMaxDurationMs(result.maxDurationMs());
        dto.setCount(result.count());
        return dto;
    }

    public List<EndpointLatencyDTO> toEndpointLatencyDTOList(List<AuditStatisticsResult.EndpointLatency> results) {
        return results.stream().map(this::toDTO).toList();
    }
}
