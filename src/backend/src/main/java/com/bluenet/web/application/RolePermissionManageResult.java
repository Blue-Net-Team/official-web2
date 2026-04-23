package com.bluenet.web.application;

import java.util.List;

/**
 * 角色权限管理聚合的应用层结果对象。
 * <p>
 * 封装了角色权限管理相关操作返回给 API 层的数据。
 * </p>
 */
public record RolePermissionManageResult(
        /** 成功数量 */
        int successCount,
        /** 当前权限列表 */
        List<String> currentPermissions,
        /** 当前角色列表 */
        List<String> currentRoles) {

    public static RolePermissionManageResult ofPermissions(int successCount, List<String> currentPermissions) {
        return new RolePermissionManageResult(successCount, currentPermissions, null);
    }

    public static RolePermissionManageResult ofRoles(int successCount, List<String> currentRoles) {
        return new RolePermissionManageResult(successCount, null, currentRoles);
    }
}
