package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_equipment")
public class EquipmentDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 设备品牌或资产品牌名称。
     */
    private String brand;

    /**
     * 业务对象的详细描述。
     */
    private String description;
    /**
     * 展示图片对应的文件记录标识。
     */
    private Long imageFileId;

    /**
     * 列表展示排序值，数值越大通常越靠前。
     */
    private Integer sortOrder;
}
