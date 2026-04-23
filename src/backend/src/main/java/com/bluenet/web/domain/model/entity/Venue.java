package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场地聚合根
 * <p>
 * 存储实验室场地信息，如办公区域、调试场地等
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Venue {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;

    /**
     * 场地名称
     */
    private String name;

    /**
     * 场地副标题
     */
    private String subtitle;

    /**
     * 场地描述
     */
    private String description;

    /**
     * 图片文件ID，关联tb_file.id
     */
    private Long imageFileId;

    /**
     * 排序权重，越大越靠前
     */
    private Integer sortOrder;

    private Venue(Long id, String name, String subtitle, String description, Long imageFileId, Integer sortOrder) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.description = description;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 构造新场地聚合根 —— 带领域校验
     */
    public static Venue create(String name, String subtitle, String description, Long imageFileId, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("场地名称不能为空");
        }
        return new Venue(null, name.trim(), subtitle, description, imageFileId, sortOrder != null ? sortOrder : 0);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static Venue reconstruct(Long id, String name, String subtitle, String description, Long imageFileId,
            Integer sortOrder) {
        return new Venue(id, name, subtitle, description, imageFileId, sortOrder);
    }

    /**
     * 更新场地信息
     */
    public void update(String name, String subtitle, String description, Long imageFileId, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("场地名称不能为空");
        }
        this.name = name.trim();
        this.subtitle = subtitle;
        this.description = description;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 更新场地图片
     */
    public void updateImage(Long imageFileId) {
        this.imageFileId = imageFileId;
    }
}
