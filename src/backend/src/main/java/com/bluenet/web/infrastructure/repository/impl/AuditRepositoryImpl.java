package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.model.vo.AuditEndpointLatencyVO;
import com.bluenet.web.domain.model.vo.AuditEndpointRankingVO;
import com.bluenet.web.domain.model.vo.AuditTrendPointVO;
import com.bluenet.web.domain.repository.AuditRepository;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import com.bluenet.web.infrastructure.repository.mapper.AuditMapper;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditEndpointLatencyQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditEndpointRankingQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditTrendPointQueryDO;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {
    private final AuditMapper auditMapper;

    /**
     * 新增一条审计日志 记录。
     *
     * @param audit
     *            审计日志领域对象。
     */
    @Override
    public void insert(Audit audit) {
        RepositoryObjectConverter.insert(auditMapper, audit, AuditDO.class);
    }

    /**
     * 按统计周期查询审计访问趋势点，供后台趋势图展示。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @return 满足条件的审计日志 结果集合。
     */
    @Override
    public List<AuditTrendPointVO> queryTrends(AuditStatisticsPeriod period) {
        return auditMapper.selectTrends(period.getValue())
                .stream()
                .map(this::toTrendPointVO)
                .toList();
    }

    /**
     * 查询接口访问次数排行，供审计统计页面展示热点接口。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    @Override
    public List<AuditEndpointRankingVO> queryEndpointRanking(AuditStatisticsPeriod period, int limit) {
        return auditMapper.selectEndpointRanking(period.getValue(), limit)
                .stream()
                .map(this::toEndpointRankingVO)
                .toList();
    }

    /**
     * 查询接口平均耗时排行，供审计统计页面定位慢接口。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    @Override
    public List<AuditEndpointLatencyVO> queryEndpointLatencyRanking(AuditStatisticsPeriod period, int limit) {
        return auditMapper.selectEndpointLatencyRanking(period.getValue(), limit)
                .stream()
                .map(this::toEndpointLatencyVO)
                .toList();
    }

    /**
     * 将审计趋势投影转换为领域统计视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AuditTrendPointVO toTrendPointVO(AuditTrendPointQueryDO row) {
        return AuditTrendPointVO.builder()
                .time(row.getTime())
                .count(row.getCount())
                .build();
    }

    /**
     * 将接口访问排行投影转换为领域统计视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AuditEndpointRankingVO toEndpointRankingVO(AuditEndpointRankingQueryDO row) {
        return AuditEndpointRankingVO.builder()
                .pattern(row.getPattern())
                .count(row.getCount())
                .avgDurationMs(row.getAvgDurationMs())
                .errorCount(row.getErrorCount())
                .build();
    }

    /**
     * 将接口耗时排行投影转换为领域统计视图对象。
     *
     * @param row
     *            Mapper 返回的投影数据行。
     * @return 转换后的目标模型对象。
     */
    private AuditEndpointLatencyVO toEndpointLatencyVO(AuditEndpointLatencyQueryDO row) {
        return AuditEndpointLatencyVO.builder()
                .pattern(row.getPattern())
                .avgDurationMs(row.getAvgDurationMs())
                .maxDurationMs(row.getMaxDurationMs())
                .count(row.getCount())
                .build();
    }
}
