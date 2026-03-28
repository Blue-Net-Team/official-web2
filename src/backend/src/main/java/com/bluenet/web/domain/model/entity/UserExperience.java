package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_experience")
public class UserExperience {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private ExperienceType type;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
