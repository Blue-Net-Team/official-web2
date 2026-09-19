package com.bluenet.web.application.result.learningpath;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 学习路径聚合的应用层结果对象。
 * <p>
 * 封装了学习路径相关操作返回给 API 层的数据。{@code sortOrder} 仅用于应用层内部传递顺序，
 * 不对外暴露——展示编号由前端按数组位置派生。
 * </p>
 */
public record LearningPathResult(
        /** 唯一标识 */
        Long id,
        /** 方向 */
        Direction direction,
        /** 展示排序值 */
        Integer sortOrder,
        /** 标题 */
        String title,
        /** 相关链接URL地址 */
        String relatedUrl) {
}
