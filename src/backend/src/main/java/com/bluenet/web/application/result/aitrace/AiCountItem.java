package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用计数项，用于意图分布、动作分布、工具使用与拒答原因。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiCountItem {
    /**
     * 计数维度名称（意图标签、动作、工具名或被拦截的意图）。
     */
    private String label;
    /**
     * 次数。
     */
    private Long count;
    /**
     * 占比（0~1），由应用层按总数计算；不适用时为 null。
     */
    private Double ratio;
}
