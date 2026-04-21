package com.bluenet.web.domain.model.entity;

import lombok.Data;

@Data
public class RolePermission {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 用户绑定的角色标识。
     */
    private Long roleId;
    /**
     * 角色权限关联中的权限标识。
     */
    private Long permissionId;
}
