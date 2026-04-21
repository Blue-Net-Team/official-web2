package com.bluenet.web.infrastructure.repository.projection;

import lombok.Data;

/**
 * 审计接口访问排名聚合查询投影，仅用于仓储查询映射。
 */
@Data
public class AuditEndpointRankingProjection {
    /**
     * 归一化后的接口路径模式。
     */
    private String pattern;
    /**
     * 统计数量。
     */
    private Long count;
    /**
     * 接口请求平均耗时，单位毫秒。
     */
    private Double avgDurationMs;
    /**
     * 失败请求或错误记录数量。
     */
    private Long errorCount;
}
