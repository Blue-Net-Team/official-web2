package com.bluenet.web.domain.model.entity;

import lombok.Data;

/**
 * 设备实体
 * <p>
 * 存储实验室设备信息，如3D打印机、激光切割机等
 * </p>
 */
@Data
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
}
