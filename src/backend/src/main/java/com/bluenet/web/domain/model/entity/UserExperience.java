package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserExperience {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 业务分类或枚举类型。
     */
    private ExperienceType type;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 经历、考核或有效期的开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 经历、考核或有效期的结束时间。
     */
    private LocalDateTime endTime;
}
