package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
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
@TableName("tb_user_experience")
public class UserExperienceDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
