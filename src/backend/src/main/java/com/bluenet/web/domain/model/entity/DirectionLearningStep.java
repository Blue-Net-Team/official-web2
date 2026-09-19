package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方向学习步骤实体
 * <p>
 * 存储各方向的学习路径步骤信息。排序值由系统分配（创建时追加到方向末尾、拖拽时整体重写）， 实体不承担"序号唯一"职责——展示编号由前端按位置派生。
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
     * 展示排序值，数值越小越靠前；允许空洞
     */
    private Integer sortOrder;

    /**
     * 步骤标题
     */
    private String title;

    /**
     * 相关链接URL
     */
    private String relatedUrl;

    private DirectionLearningStep(Long id, Direction direction, Integer sortOrder, String title, String relatedUrl) {
        this.id = id;
        this.direction = direction;
        this.sortOrder = sortOrder;
        this.title = title;
        this.relatedUrl = relatedUrl;
    }

    /**
     * 构造新学习步骤 —— 带领域校验
     * <p>
     * 排序值由调用方（应用层）系统分配，不接受用户直接输入。
     * </p>
     */
    public static DirectionLearningStep create(Direction direction, Integer sortOrder, String title,
            String relatedUrl) {
        if (direction == null) {
            throw new IllegalArgumentException("方向不能为空");
        }
        if (sortOrder == null || sortOrder < 1) {
            throw new IllegalArgumentException("步骤顺序值必须大于0");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        return new DirectionLearningStep(null, direction, sortOrder, title.trim(), relatedUrl);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static DirectionLearningStep reconstruct(Long id, Direction direction, Integer sortOrder, String title,
            String relatedUrl) {
        return new DirectionLearningStep(id, direction, sortOrder, title, relatedUrl);
    }

    /**
     * 更新展示排序值
     */
    public void updateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 1) {
            throw new IllegalArgumentException("步骤顺序值必须大于0");
        }
        this.sortOrder = sortOrder;
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
     * 更新相关链接
     */
    public void updateRelatedUrl(String relatedUrl) {
        this.relatedUrl = relatedUrl;
    }
}
