package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计趋势聚合查询数据对象，仅用于承接 XML 查询结果。
 */
@Data
public class AuditTrendPointQueryDO {
    /**
     * 趋势统计中的时间点。
     */
    private LocalDateTime time;
    /**
     * 统计数量。
     */
    private Long count;
}
