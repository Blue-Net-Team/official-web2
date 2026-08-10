package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Role;

import java.util.Optional;

/**
 * 角色仓储接口
 * <p>
 * 提供角色相关的数据访问抽象
 * </p>
 */
public interface RoleRepository {

    /**
     * 按主键查询角色实体。
     *
     * @param id
     *            角色主键。
     * @return 查询到的角色实体；不存在时为空。
     */
    Optional<Role> findById(Long id);

    /**
     * 按名称查询角色 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 查询到的角色 结果；不存在时为空。
     */
    Optional<Role> findByName(String name);
}
