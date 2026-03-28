package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_assessment_time")
public class AssessmentTime {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Direction direction;
    private Integer epoch;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean timeLimit;
    private Integer timeLimitMinutes;
}
