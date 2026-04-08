package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_assessment_session")
public class AssessmentSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long assessmentTimeId;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
}
