package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEndpointLatencyVO {
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
