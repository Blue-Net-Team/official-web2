package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方向学习步骤实体
 * <p>
 * 存储各方向的学习路径步骤信息
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectionLearningStep {

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

    private DirectionLearningStep(Long id, Direction direction, Integer stepNumber, String title, String videoUrl) {
        this.id = id;
        this.direction = direction;
        this.stepNumber = stepNumber;
        this.title = title;
        this.videoUrl = videoUrl;
    }

    /**
     * 构造新学习步骤 —— 带领域校验
     */
    public static DirectionLearningStep create(Direction direction, Integer stepNumber, String title, String videoUrl) {
        if (direction == null) {
            throw new IllegalArgumentException("方向不能为空");
        }
        if (stepNumber == null || stepNumber < 1) {
            throw new IllegalArgumentException("步骤序号必须大于0");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        return new DirectionLearningStep(null, direction, stepNumber, title.trim(), videoUrl);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static DirectionLearningStep reconstruct(Long id, Direction direction, Integer stepNumber, String title,
            String videoUrl) {
        return new DirectionLearningStep(id, direction, stepNumber, title, videoUrl);
    }

    /**
     * 更新步骤序号
     */
    public void updateStepNumber(Integer stepNumber) {
        if (stepNumber == null || stepNumber < 1) {
            throw new IllegalArgumentException("步骤序号必须大于0");
        }
        this.stepNumber = stepNumber;
    }

    /**
     * 更新标题
     */
    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        this.title = title.trim();
    }

    /**
     * 更新视频链接
     */
    public void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
