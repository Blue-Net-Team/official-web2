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
    private Long total;
    private Map<String, Long> byStatus;
    private Map<Direction, Long> byDirection;
}
