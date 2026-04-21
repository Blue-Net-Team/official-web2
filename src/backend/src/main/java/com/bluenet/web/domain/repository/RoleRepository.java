package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.RoleVO;

import java.util.Optional;

/**
 * 角色仓储接口
 * <p>
 * 提供角色相关的数据访问抽象
 * </p>
 */
public interface RoleRepository {

    /**
     * 按名称查询角色 记录。
     *
     * @param name
     *            业务对象名称。
     * @return 查询到的角色 结果；不存在时为空。
     */
    Optional<RoleVO> findByName(String name);
}
