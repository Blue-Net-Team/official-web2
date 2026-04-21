package com.bluenet.web.infrastructure.repository.projection;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计趋势聚合查询投影，仅用于仓储查询映射。
 */
@Data
public class AuditTrendPointProjection {
    /**
     * 趋势统计中的时间点。
     */
    private LocalDateTime time;
    /**
     * 统计数量。
     */
    private Long count;
}
