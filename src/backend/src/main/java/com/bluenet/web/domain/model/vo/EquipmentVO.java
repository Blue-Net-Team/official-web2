package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备值对象
 * <p>
 * 封装设备完整信息，用于领域层传递
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentVO {
    /**
     * 设备ID
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
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片文件ID
     */
    private Long imageFileId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
