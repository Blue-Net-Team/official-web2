package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
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
@TableName("tb_direction_learning_step")
public class DirectionLearningStepDO {
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
     * 学习路径步骤序号。
     */
    private Integer stepNumber;

    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 视频演示或介绍地址。
     */
    private String videoUrl;
}
