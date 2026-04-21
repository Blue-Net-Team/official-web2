package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Audit;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.domain.model.vo.AuditEndpointLatencyVO;
import com.bluenet.web.domain.model.vo.AuditEndpointRankingVO;
import com.bluenet.web.domain.model.vo.AuditTrendPointVO;

import java.util.List;

/**
 * 审计日志仓库接口
 */
public interface AuditRepository {
    /**
     * 新增一条审计日志 记录。
     *
     * @param audit
     *            审计日志领域对象。
     */
    void insert(Audit audit);

    /**
     * 按统计周期查询审计访问趋势点，供后台趋势图展示。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @return 审计访问趋势点列表（按时间顺序）
     */
    List<AuditTrendPointVO> queryTrends(AuditStatisticsPeriod period);

    /**
     * 查询接口访问次数排行，供审计统计页面展示热点接口。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    List<AuditEndpointRankingVO> queryEndpointRanking(AuditStatisticsPeriod period, int limit);

    /**
     * 查询接口平均耗时排行，供审计统计页面定位慢接口。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    List<AuditEndpointLatencyVO> queryEndpointLatencyRanking(AuditStatisticsPeriod period, int limit);
}
