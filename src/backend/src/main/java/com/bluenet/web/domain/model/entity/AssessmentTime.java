package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssessmentTime {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 考核批次或轮次编号。
     */
    private Integer epoch;
    /**
     * 学生年级或成绩等级。
     */
    private Integer grade;
    /**
     * 经历、考核或有效期的开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 经历、考核或有效期的结束时间。
     */
    private LocalDateTime endTime;
    /**
     * 算法题时间限制，通常以毫秒为单位。
     */
    private Boolean timeLimit;
    /**
     * 考核作答时长限制，单位分钟。
     */
    private Integer timeLimitMinutes;
}
