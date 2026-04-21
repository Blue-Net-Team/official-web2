package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrendPointVO {
    /**
     * 趋势统计中的时间点。
     */
    private LocalDateTime time;
    /**
     * 统计数量。
     */
    private Long count;
}
