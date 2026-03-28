package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import java.util.regex.Pattern;

/**
 * 权限校验工具类 用于验证权限注解的格式和合法性
 */
public class PermissionValidator {

    /**
     * 权限值格式正则表达式 格式: resource:action (小写字母) 示例: user:create,
     * assessment-time:view-self
     */
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("^[a-z-]+(:[a-z-]+)+$");

    /**
     * 验证权限值格式
     *
     * @param value
     *            权限值
     * @return true 如果格式正确
     */
    public static boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return PERMISSION_PATTERN.matcher(value).matches();
    }

    /**
     * 验证权限注解 抛出异常如果验证失败
     *
     * @param requiresPermission
     *            权限注解
     * @param className
     *            类名（用于错误信息）
     * @param methodName
     *            方法名（用于错误信息）
     * @throws IllegalArgumentException
     *             如果验证失败
     */
    public static void validate(RequiresPermission requiresPermission, String className, String methodName) {
        if (requiresPermission == null) {
            return;
        }
        validate(requiresPermission.value(), className, methodName);
    }

    /**
     * 直接验证权限值 抛出异常如果验证失败
     *
     * @param value
     *            权限值
     * @param className
     *            类名（用于错误信息）
     * @param methodName
     *            方法名（用于错误信息）
     * @throws IllegalArgumentException
     *             如果验证失败
     */
    public static void validate(String value, String className, String methodName) {
        // 检查值是否为空
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Permission value cannot be empty. Class: %s, Method: %s", className, methodName));
        }

        // 检查格式
        if (!isValid(value)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid permission value '%s'. Must match pattern '[resource:action]' (lowercase letters only). Class: %s, Method: %s",
                    value,
                    className,
                    methodName));
        }
    }
}
