package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.StatisticsPeriod;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.domain.model.entity.Audit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditMapper extends BaseMapper<Audit> {
    List<TrendPointDTO> selectTrends(@Param("period") StatisticsPeriod period);

    List<EndpointRankingDTO> selectEndpointRanking(@Param("period") StatisticsPeriod period, @Param("limit") int limit);

    List<EndpointLatencyDTO> selectEndpointLatencyRanking(@Param("period") StatisticsPeriod period,
            @Param("limit") int limit);
}
