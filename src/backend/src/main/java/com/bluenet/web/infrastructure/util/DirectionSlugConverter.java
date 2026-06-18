package com.bluenet.web.infrastructure.util;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 方向Slug转换工具类
 * <p>
 * 负责前端slug与后端Direction枚举之间的映射
 * </p>
 */
public class DirectionSlugConverter {

    /**
     * 将前端slug转换为Direction枚举
     *
     * @param slug
     *            前端slug（cv/embed/struct）
     * @return Direction枚举
     * @throws IllegalArgumentException
     *             如果slug为空或null
     * @throws DataNotFound
     *             如果slug无效
     */
    public static Direction fromSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("方向标识不能为空");
        }

        return switch (slug.toLowerCase()) {
            case "cv" -> Direction.COMPUTER_VISION;
            case "embed" -> Direction.EMBEDDED;
            case "struct" -> Direction.STRUCTURAL_DESIGN;
            default -> throw new DataNotFound("无效的方向标识: " + slug);
        };
    }

    /**
     * 将Direction枚举转换为前端slug
     *
     * @param direction
     *            Direction枚举
     * @return 前端slug
     * @throws IllegalArgumentException
     *             如果direction为null
     */
    public static String toSlug(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("方向不能为空");
        }

        return switch (direction) {
            case COMPUTER_VISION -> "cv";
            case EMBEDDED -> "embed";
            case STRUCTURAL_DESIGN -> "struct";
            case GENERAL -> "general";
        };
    }
}
