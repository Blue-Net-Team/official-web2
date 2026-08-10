package com.bluenet.web.application.query.permission;

/**
 * 查询权限列表查询参数。
 */
public record GetPermissionsQuery(
        /** 关键词 */
        String keyword,
        /** 格式 */
        String format,
        /** 页码 */
        Integer page,
        /** 每页大小 */
        Integer size) {
}
