package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备聚合根
 * <p>
 * 存储实验室设备信息，如3D打印机、激光切割机等
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Equipment {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备品牌
     */
    private String brand;

    /**
     * 设备描述
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

    private Equipment(Long id, String name, String brand, String description, Long imageFileId, Integer sortOrder) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 构造新设备聚合根 —— 带领域校验
     */
    public static Equipment create(String name, String brand, String description, Long imageFileId, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        return new Equipment(null, name.trim(), brand, description, imageFileId, sortOrder != null ? sortOrder : 0);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static Equipment reconstruct(Long id, String name, String brand, String description, Long imageFileId,
            Integer sortOrder) {
        return new Equipment(id, name, brand, description, imageFileId, sortOrder);
    }

    /**
     * 更新设备信息
     */
    public void update(String name, String brand, String description, Long imageFileId, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        this.name = name.trim();
        this.brand = brand;
        this.description = description;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 更新设备图片
     */
    public void updateImage(Long imageFileId) {
        this.imageFileId = imageFileId;
    }
}
