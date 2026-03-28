package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.domain.model.vo.UserVO;
import org.jetbrains.annotations.Nullable;

/**
 * 用户上下文 存储当前登录用户信息（ThreadLocal实现）
 */
public class UserCTX {

    /**
     * 当前用户信息
     */
    private static final ThreadLocal<UserVO> currentUser = new ThreadLocal<>();

    /**
     * 设置当前用户
     *
     * @param user
     *            用户信息
     */
    public static void setCurrentUser(UserVO user) {
        currentUser.set(user);
    }

    /**
     * 获取当前用户
     *
     * @return 用户信息，如果未登录返回null
     */
    @Nullable
    public static UserVO getCurrentUser() {
        return currentUser.get();
    }

    /**
     * 清除当前用户 必须在请求结束后调用，防止内存泄漏
     */
    public static void clear() {
        currentUser.remove();
    }

    /**
     * 检查是否已登录
     *
     * @return true 如果已登录
     */
    public static boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    @Nullable
    public static Long getCurrentUserId() {
        UserVO user = currentUser.get();
        if (user != null) {
            return user.getId();
        }
        return null;
    }

    /**
     * 获取当前用户角色名
     *
     * @return 角色ID
     */
    @Nullable
    public static String getCurrentRoleId() {
        UserVO user = currentUser.get();
        if (user == null) {
            return null;
        }
        return user.getRoleName();
    }
}
