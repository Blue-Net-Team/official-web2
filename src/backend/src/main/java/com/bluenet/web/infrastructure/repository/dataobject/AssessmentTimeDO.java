package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_assessment_time")
public class AssessmentTimeDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
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

    /**
     * 考核结果发布时间，设置后考生可见评论和最终评分。
     */
    private LocalDateTime resultsPublishedAt;

    /**
     * 是否允许组队答题。
     */
    private Boolean allowTeam;
}
