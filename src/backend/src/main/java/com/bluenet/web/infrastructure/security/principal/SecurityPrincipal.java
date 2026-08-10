package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;

import java.util.Collections;
import java.util.Set;

/**
 * 安全上下文主体，封装当前认证用户的核心安全属性。
 * <p>
 * 包含 {@link User} 实体、解析后的 {@link RoleType} 以及该用户拥有的权限集合。 由
 * {@link com.bluenet.web.infrastructure.security.jwt.JwtAuthenticationFilter}
 * 在每次请求开始时构造并写入 {@link com.bluenet.web.infrastructure.security.util.UserCTX}。
 * </p>
 *
 * @param user
 *            当前用户实体
 * @param roleType
 *            解析后的角色类型
 * @param permissions
 *            当前用户的权限值集合，不可变
 */
public record SecurityPrincipal(User user, RoleType roleType, Set<String> permissions) {

    /**
     * 构造安全上下文主体，权限集合会被包装为不可变集合。
     */
    public SecurityPrincipal {
        permissions = permissions != null ? Set.copyOf(permissions) : Collections.emptySet();
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID，如果用户为空则返回 null
     */
    public Long userId() {
        return user != null ? user.getId() : null;
    }

    /**
     * 判断当前主体是否拥有指定权限。
     *
     * @param permission
     *            权限值
     * @return true 如果拥有该权限
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
