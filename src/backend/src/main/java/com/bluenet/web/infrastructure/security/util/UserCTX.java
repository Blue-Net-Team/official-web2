package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * 用户上下文，基于 ThreadLocal 存储当前请求的安全主体 {@link SecurityPrincipal}。
 * <p>
 * 每个请求在
 * {@link com.bluenet.web.infrastructure.security.jwt.JwtAuthenticationFilter}
 * 中构造 {@link SecurityPrincipal} 并写入本上下文，请求结束后必须调用 {@link #clear()} 清理， 防止内存泄漏。
 * </p>
 */
public class UserCTX {

    private static final ThreadLocal<SecurityPrincipal> currentPrincipal = new ThreadLocal<>();

    /**
     * 设置当前安全主体。
     *
     * @param principal
     *            安全主体
     */
    public static void setPrincipal(SecurityPrincipal principal) {
        currentPrincipal.set(principal);
    }

    /**
     * 获取当前安全主体。
     *
     * @return 当前安全主体，未登录时返回 null
     */
    @Nullable
    public static SecurityPrincipal getPrincipal() {
        return currentPrincipal.get();
    }

    /**
     * 获取当前用户实体。
     *
     * @return 当前用户实体，未登录时返回 null
     */
    @Nullable
    public static User getCurrentUser() {
        SecurityPrincipal principal = currentPrincipal.get();
        return principal != null ? principal.user() : null;
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID，未登录时返回 null
     */
    @Nullable
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户角色类型。
     *
     * @return 角色类型，未登录或角色未知时返回 null
     */
    @Nullable
    public static RoleType getCurrentRoleType() {
        SecurityPrincipal principal = currentPrincipal.get();
        return principal != null ? principal.roleType() : null;
    }

    /**
     * 获取当前用户权限集合。
     *
     * @return 权限集合，未登录时返回空集合
     */
    public static Set<String> getCurrentPermissions() {
        SecurityPrincipal principal = currentPrincipal.get();
        return principal != null ? principal.permissions() : Collections.emptySet();
    }

    /**
     * 检查是否已登录。
     *
     * @return true 如果已登录
     */
    public static boolean isAuthenticated() {
        return currentPrincipal.get() != null;
    }

    /**
     * 清除当前安全主体。必须在请求结束后调用，防止内存泄漏。
     */
    public static void clear() {
        currentPrincipal.remove();
    }
}
