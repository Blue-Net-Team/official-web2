package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色权限关联实体
 * <p>
 * 承载角色与权限关联关系的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    private RolePermission(Long id, Long roleId, Long permissionId) {
        this.id = id;
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    /**
     * 构造新角色权限关联 —— 带领域校验
     *
     * @param roleId
     *            角色ID
     * @param permissionId
     *            权限ID
     * @return 新的角色权限关联实体
     * @throws IllegalArgumentException
     *             如果角色ID或权限ID为空
     */
    public static RolePermission create(Long roleId, Long permissionId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        return new RolePermission(null, roleId, permissionId);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            关联ID
     * @param roleId
     *            角色ID
     * @param permissionId
     *            权限ID
     * @return 重建的角色权限关联实体
     */
    public static RolePermission reconstruct(Long id, Long roleId, Long permissionId) {
        return new RolePermission(id, roleId, permissionId);
    }
}
