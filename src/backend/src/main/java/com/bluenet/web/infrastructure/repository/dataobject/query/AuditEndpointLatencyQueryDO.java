package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

/**
 * 审计接口耗时排名聚合查询数据对象，仅用于承接 XML 查询结果。
 */
@Data
public class AuditEndpointLatencyQueryDO {
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
