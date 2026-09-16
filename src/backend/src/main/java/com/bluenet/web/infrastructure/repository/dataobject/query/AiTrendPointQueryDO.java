package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话量趋势投影查询数据对象，仅用于承接 XML 查询结果。
 */
@Data
public class AiTrendPointQueryDO {
    /**
     * 时间桶起始时间。
     */
    private LocalDateTime time;
    /**
     * 该时间桶内新增的会话数量。
     */
    private Long count;
}
