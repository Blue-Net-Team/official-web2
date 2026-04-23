package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限聚合根
 * <p>
 * 承载权限相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    private Permission(Long id, String name, String value, String url, String method, String accessLevel) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.url = url;
        this.method = method;
        this.accessLevel = accessLevel;
    }

    /**
     * 构造新权限 —— 带领域校验
     *
     * @param name
     *            权限名称
     * @param value
     *            权限标识符
     * @param url
     *            资源访问地址
     * @param method
     *            HTTP 方法
     * @param accessLevel
     *            访问级别
     * @return 新的权限实体
     * @throws IllegalArgumentException
     *             如果权限标识符为空
     */
    public static Permission create(String name, String value, String url, String method, String accessLevel) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("权限标识符不能为空");
        }
        return new Permission(null, name, value, url, method, accessLevel);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            权限ID
     * @param name
     *            权限名称
     * @param value
     *            权限标识符
     * @param url
     *            资源访问地址
     * @param method
     *            HTTP 方法
     * @param accessLevel
     *            访问级别
     * @return 重建的权限实体
     */
    public static Permission reconstruct(Long id, String name, String value, String url, String method,
            String accessLevel) {
        return new Permission(id, name, value, url, method, accessLevel);
    }
}
