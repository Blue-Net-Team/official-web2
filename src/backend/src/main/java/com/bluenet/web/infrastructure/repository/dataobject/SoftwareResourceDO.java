package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
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
@TableName("tb_software_resource")
public class SoftwareResourceDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 软件名称。
     */
    private String name;

    /**
     * 所属方向。
     */
    private SoftwareResourceDirection direction;

    /**
     * 分类。
     */
    private String category;

    /**
     * 描述。
     */
    private String description;

    /**
     * 外部下载链接。
     */
    private String externalUrl;

    /**
     * 排序权重。
     */
    private Integer sortOrder;

    /**
     * 启用状态。
     */
    private SoftwareResourceStatus status;
}
