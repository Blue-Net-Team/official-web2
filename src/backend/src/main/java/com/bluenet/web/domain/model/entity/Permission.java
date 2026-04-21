package com.bluenet.web.domain.model.entity;

import lombok.Data;

@Data
public class Permission {
    /**
     * 当前对象在系统中的唯一标识。
     */
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
