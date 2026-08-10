package com.bluenet.web.application.result.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrendPoint {
    /**
     * 趋势统计中的时间点。
     */
    private LocalDateTime time;
    /**
     * 统计数量。
     */
    private Long count;
}
