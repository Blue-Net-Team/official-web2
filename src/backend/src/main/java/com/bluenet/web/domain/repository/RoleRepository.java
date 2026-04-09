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
     * 根据角色名称查找角色
     *
     * @param name
     *            角色名称（如 CANDIDATE, MEMBER 等）
     * @return 角色 VO，未找到返回 empty
     */
    Optional<RoleVO> findByName(String name);
}
