package com.bluenet.web.infrastructure.adapter;

import io.github.ivencn.infra.security.principal.SecurityPrincipal;
import lombok.experimental.UtilityClass;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;

/**
 * 当前登录用户上下文（应用侧）。
 *
 * <p>
 * 框架 {@link io.github.ivencn.infra.security.principal.UserCTX} 只暴露无业务依赖的主体；
 * 本类在其上提供领域用户实体与角色类型的便捷访问。由 {@link IvenUserDetailsLoader} 在认证时写入，由
 * {@link LoginContextCleanupFilter} 在请求结束时清理。
 * </p>
 */
@UtilityClass
public class LoginContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<RoleType> currentRoleType = new ThreadLocal<>();

    /**
     * 认证加载用户详情时写入。
     */
    public static void set(User user, RoleType roleType) {
        currentUser.set(user);
        currentRoleType.set(roleType);
    }

    /**
     * 获取当前用户实体。
     *
     * @return 当前用户，未登录时返回 null
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 获取当前用户角色类型。
     *
     * @return 角色类型，未登录或未知时返回 null
     */
    public static RoleType getCurrentRoleType() {
        return currentRoleType.get();
    }

    /**
     * 获取当前主体（框架）。
     */
    public static SecurityPrincipal getPrincipal() {
        return io.github.ivencn.infra.security.principal.UserCTX.getPrincipal();
    }

    /**
     * 请求结束后清理。
     */
    public static void clear() {
        currentUser.remove();
        currentRoleType.remove();
    }
}
