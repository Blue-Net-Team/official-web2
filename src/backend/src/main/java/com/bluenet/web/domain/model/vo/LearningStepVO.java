package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习步骤值对象
 * <p>
 * 用于领域层传递学习步骤信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LearningStepVO {
    /**
     * 步骤ID
     */
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
