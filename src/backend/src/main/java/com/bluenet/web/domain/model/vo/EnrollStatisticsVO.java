package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class EnrollStatisticsVO {
    /**
     * 统计总数。
     */
    private Long total;
    /**
     * 按业务状态聚合后的统计结果。
     */
    private Map<String, Long> byStatus;
    /**
     * 按技术方向聚合后的统计结果。
     */
    private Map<Direction, Long> byDirection;
}
