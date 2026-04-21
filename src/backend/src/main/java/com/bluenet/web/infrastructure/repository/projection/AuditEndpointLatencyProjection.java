package com.bluenet.web.infrastructure.repository.projection;

import lombok.Data;

/**
 * 审计接口耗时排名聚合查询投影，仅用于仓储查询映射。
 */
@Data
public class AuditEndpointLatencyProjection {
    /**
     * 归一化后的接口路径模式。
     */
    private String pattern;
    /**
     * 接口请求平均耗时，单位毫秒。
     */
    private Double avgDurationMs;
    /**
     * 接口请求最大耗时，单位毫秒。
     */
    private Long maxDurationMs;
    /**
     * 统计数量。
     */
    private Long count;
}
