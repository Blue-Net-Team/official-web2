package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.Data;

/**
 * 方向学习步骤实体
 * <p>
 * 存储各方向的学习路径步骤信息
 * </p>
 */
@Data
@TableName("tb_direction_learning_step")
public class DirectionLearningStep {
    /**
     * 步骤ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 方向
     */
    private Direction direction;

    /**
     * 步骤序号
     */
    private Integer stepNumber;

    /**
     * 步骤标题
     */
    private String title;

    /**
     * 视频链接URL
     */
    private String videoUrl;
}
