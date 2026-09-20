package com.bluenet.web.testsupport.fixture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;

/**
 * 角色测试夹具。
 *
 * <p>
 * 角色 ID 由 {@code SERIAL}
 * 生成，迁移脚本只写入角色名称（{@code INSERT INTO tb_role (name) VALUES (...)}）， 因此 ID
 * 不可假定为固定值；生产代码也从不按 ID 查找角色。本夹具因此不提供任何硬编码角色 ID 的快捷方法。
 * </p>
 *
 * <p>
 * 集成测试有两种按名称解析的方式：{@link #roleId(RoleMapper, RoleType)} 直接查询数据库；
 * {@link #roleIdFor(RoleType)} 读取由 {@code DBIntegrationTest} 在迁移完成后注入的角色名到 ID
 * 映射， 适合没有 {@code RoleMapper} 的测试夹具内部使用。不访问数据库的纯单元测试请使用
 * {@code UserFixture.Builder.withRoleId(Long)} 显式指定。
 * </p>
 */
public final class RoleFixture {

    /** 由 DBIntegrationTest 在迁移完成后注入的角色名到 ID 映射。 */
    private static final Map<RoleType, Long> BOUND_ROLE_IDS = new ConcurrentHashMap<>();

    private RoleFixture() {
    }

    /**
     * 注入角色名到 ID 的映射。
     *
     * @param roleIds
     *            角色类型到真实 ID 的映射。
     */
    public static void bindRoleIds(Map<RoleType, Long> roleIds) {
        BOUND_ROLE_IDS.clear();
        BOUND_ROLE_IDS.putAll(roleIds);
    }

    /**
     * 清除注入的角色映射，避免泄漏到不访问数据库的单元测试。
     */
    public static void unbindRoleIds() {
        BOUND_ROLE_IDS.clear();
    }

    /**
     * 读取已注入映射中的角色 ID。
     *
     * @param roleType
     *            角色类型。
     * @return 该角色的真实 ID。
     * @throws IllegalStateException
     *             未注入映射时抛出，通常意味着在纯单元测试中误用了依赖数据库的夹具。
     */
    public static Long roleIdFor(RoleType roleType) {
        Long roleId = BOUND_ROLE_IDS.get(roleType);
        if (roleId == null) {
            throw new IllegalStateException("未绑定角色 ID：" + roleType
                    + "。集成测试请继承 DBIntegrationTest（基类会注入角色映射）；"
                    + "纯单元测试请使用 UserFixture.Builder.withRoleId(Long) 显式指定。");
        }
        return roleId;
    }

    /**
     * 从数据库按名称查询角色 ID。
     *
     * @param roleMapper
     *            角色数据行 Mapper。
     * @param roleType
     *            角色类型。
     * @return 该角色的真实 ID。
     */
    public static Long roleId(RoleMapper roleMapper, RoleType roleType) {
        RoleDO role = roleMapper.selectByName(roleType.getName());
        if (role == null) {
            throw new IllegalStateException("角色不存在: " + roleType.getName());
        }
        return role.getId();
    }

    /**
     * 从数据库按名称查询角色 DO。
     *
     * @param roleMapper
     *            角色数据行 Mapper。
     * @param roleType
     *            角色类型。
     * @return 匹配的角色数据行；不存在时为 null。
     */
    public static RoleDO role(RoleMapper roleMapper, RoleType roleType) {
        return roleMapper.selectByName(roleType.getName());
    }
}
