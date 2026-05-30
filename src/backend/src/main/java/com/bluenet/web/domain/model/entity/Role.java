package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Role {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;

    /**
     * 构造新角色聚合根
     *
     * @param name
     *            角色名称
     * @return 新的角色实体
     * @throws IllegalArgumentException
     *             如果名称为空
     */
    public static Role create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        Role role = new Role();
        role.setName(name.trim());
        return role;
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            角色ID
     * @param name
     *            角色名称
     * @return 重建的角色实体
     */
    public static Role reconstruct(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}
