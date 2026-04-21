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
@TableName("tb_permission")
public class PermissionDO {
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
     * 权限值或业务枚举值。
     */
    private String value;

    /**
     * 资源访问地址。
     */
    private String url;
    /**
     * 权限或审计记录对应的 HTTP 方法。
     */
    private String method;

    /**
     * 权限访问级别，用于区分公开、登录后访问或更高权限访问。
     */
    private String accessLevel;
}
