package com.bluenet.web.api.converter.auditstatistics;

import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.application.result.audit.AuditEndpointLatency;
import com.bluenet.web.application.result.audit.AuditEndpointRanking;
import com.bluenet.web.application.result.audit.AuditTrendPoint;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 审计统计响应转换器
 * <p>
 * 负责将审计统计 Result 转换为接口 DTO
 * </p>
 */
@Component
public class AuditStatisticsResponseConverter {

    public TrendPointDTO toDTO(AuditTrendPoint result) {
        TrendPointDTO dto = new TrendPointDTO();
        dto.setTime(result.getTime());
        dto.setCount(result.getCount());
        return dto;
    }

    public List<TrendPointDTO> toTrendPointDTOList(List<AuditTrendPoint> results) {
        return results.stream().map(this::toDTO).toList();
    }

    public EndpointRankingDTO toDTO(AuditEndpointRanking result) {
        EndpointRankingDTO dto = new EndpointRankingDTO();
        dto.setPattern(result.getPattern());
        dto.setCount(result.getCount());
        dto.setAvgDurationMs(result.getAvgDurationMs());
        dto.setErrorCount(result.getErrorCount());
        return dto;
    }

    public List<EndpointRankingDTO> toEndpointRankingDTOList(List<AuditEndpointRanking> results) {
        return results.stream().map(this::toDTO).toList();
    }

    public EndpointLatencyDTO toDTO(AuditEndpointLatency result) {
        EndpointLatencyDTO dto = new EndpointLatencyDTO();
        dto.setPattern(result.getPattern());
        dto.setAvgDurationMs(result.getAvgDurationMs());
        dto.setMaxDurationMs(result.getMaxDurationMs());
        dto.setCount(result.getCount());
        return dto;
    }

    public List<EndpointLatencyDTO> toEndpointLatencyDTOList(List<AuditEndpointLatency> results) {
        return results.stream().map(this::toDTO).toList();
    }
}
