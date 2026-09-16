package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话量趋势数据点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTrendPoint {
    /**
     * 时间桶起始时间。
     */
    private LocalDateTime time;
    /**
     * 该时间桶内新增的会话数量。
     */
    private Long count;
}
