package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 场地实体
 * <p>
 * 存储实验室场地信息，如办公区域、调试场地等
 * </p>
 */
@Data
@TableName("tb_venue")
public class Venue {
    @TableId(type = IdType.AUTO)
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
}
