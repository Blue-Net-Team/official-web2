package com.bluenet.web.application.result.permission;

import java.util.List;

/**
 * 权限聚合的应用层结果对象。
 * <p>
 * 封装了权限相关操作返回给 API 层的数据。
 * </p>
 */
public record PermissionResult(
        /** 唯一标识 */
        Long id,
        /** 权限值 */
        String value,
        /** 名称 */
        String name,
        /** URL地址 */
        String url,
        /** 请求方法 */
        String method,
        /** 访问级别 */
        String accessLevel,
        /** 已分配角色列表 */
        List<String> assignedRoles) {

    public PermissionResult(Long id, String value, String name, String url, String method, String accessLevel) {
        this(id, value, name, url, method, accessLevel, List.of());
    }
}
