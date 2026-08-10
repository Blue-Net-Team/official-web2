package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;

/**
 * 角色测试夹具。
 *
 * <p>
 * 集成测试中应使用 {@link #roleId(RoleMapper, RoleType)} 从数据库查询真实角色 ID； 纯单元测试可调用
 * {@link #defaultRoleId(RoleType)} 使用 Flyway 初始化后的默认 ID。
 * </p>
 */
public final class RoleFixture {

    private RoleFixture() {
    }

    /**
     * 根据角色类型返回默认角色 ID（与 Flyway V1 初始化数据一致）。
     *
     * <p>
     * 仅用于不依赖数据库的单元测试。集成测试请使用 {@link #roleId(RoleMapper, RoleType)}。
     * </p>
     */
    public static Long defaultRoleId(RoleType roleType) {
        return switch (roleType) {
            case SUPER_ADMIN -> 1L;
            case DIRECTION_ADMIN -> 2L;
            case MEMBER -> 3L;
            case CANDIDATE -> 4L;
        };
    }

    /**
     * 从数据库查询角色 ID。
     */
    public static Long roleId(RoleMapper roleMapper, RoleType roleType) {
        RoleDO role = roleMapper.selectByName(roleType.getName());
        if (role == null) {
            throw new IllegalStateException("角色不存在: " + roleType.getName());
        }
        return role.getId();
    }

    /**
     * 从数据库查询角色 DO。
     */
    public static RoleDO role(RoleMapper roleMapper, RoleType roleType) {
        return roleMapper.selectByName(roleType.getName());
    }
}
