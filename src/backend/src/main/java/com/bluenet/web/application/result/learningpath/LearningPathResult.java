package com.bluenet.web.application.result.learningpath;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 学习路径聚合的应用层结果对象。
 * <p>
 * 封装了学习路径相关操作返回给 API 层的数据。
 * </p>
 */
public record LearningPathResult(
        /** 唯一标识 */
        Long id,
        /** 方向 */
        Direction direction,
        /** 步骤序号 */
        Integer stepNumber,
        /** 标题 */
        String title,
        /** 视频URL地址 */
        String videoUrl) {
}
