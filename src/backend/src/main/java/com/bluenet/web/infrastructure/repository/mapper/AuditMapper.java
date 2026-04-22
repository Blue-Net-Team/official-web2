package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AuditDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditEndpointLatencyQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditEndpointRankingQueryDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AuditTrendPointQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditMapper extends BaseMapper<AuditDO> {
    /**
     * 查询审计日志 数据行。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @return 满足条件的审计日志 结果集合。
     */
    List<AuditTrendPointQueryDO> selectTrends(@Param("period") String period);

    /**
     * 查询审计日志 数据行。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    List<AuditEndpointRankingQueryDO> selectEndpointRanking(@Param("period") String period,
            @Param("limit") int limit);

    /**
     * 查询审计日志 数据行。
     *
     * @param period
     *            统计周期，例如按日、按周或按月。
     * @param limit
     *            最大返回数量。
     * @return 满足条件的审计日志 结果集合。
     */
    List<AuditEndpointLatencyQueryDO> selectEndpointLatencyRanking(@Param("period") String period,
            @Param("limit") int limit);
}
