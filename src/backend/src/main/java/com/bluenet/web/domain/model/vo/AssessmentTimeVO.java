package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试时间领域值对象
 * <p>
 * 封装考试时间相关的领域数据，用于在领域层传递考试时间信息。
 * </p>
 */
@Data
@Builder
public class AssessmentTimeVO {
    /**
     * 考试时间ID
     */
    private Long id;

    /**
     * 方向
     */
    private Direction direction;

    /**
     * 届次
     */
    private Integer epoch;

    /**
     * 年级（1=大一, 2=大二, 3=大三）
     */
    private Integer grade;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否限制时间
     */
    private Boolean timeLimit;

    /**
     * 时间限制分钟数
     */
    private Integer timeLimitMinutes;
}
