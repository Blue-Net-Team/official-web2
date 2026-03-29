package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设备实体
 * <p>
 * 存储实验室设备信息，如3D打印机、激光切割机等
 * </p>
 */
@Data
@TableName("tb_equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
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
