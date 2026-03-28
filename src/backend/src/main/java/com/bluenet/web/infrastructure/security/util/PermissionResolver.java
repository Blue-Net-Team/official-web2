package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import lombok.Getter;
import org.springframework.web.method.HandlerMethod;

/**
 * 权限解析工具类 处理类级与方法级权限注解的优先级规则
 */
public class PermissionResolver {

    /**
     * 权限信息包装类
     */
    public static class PermissionInfo {
        @Getter
        private final String value;
        @Getter
        private final String name;
        @Getter
        private final AccessLevel access;
        private final boolean hasPermission;

        public PermissionInfo(String value, String name, AccessLevel access, boolean hasPermission) {
            this.value = value;
            this.name = name;
            this.access = access;
            this.hasPermission = hasPermission;
        }

        public boolean hasPermission() {
            return hasPermission;
        }
    }

    /**
     * 解析 HandlerMethod 的权限信息 优先级: 方法级 > 类级
     *
     * @param handlerMethod
     *            Spring HandlerMethod
     * @return PermissionInfo 权限信息（如果没有权限注解，返回 hasPermission=false）
     */
    public static PermissionInfo resolve(HandlerMethod handlerMethod) {
        if (handlerMethod == null) {
            return new PermissionInfo(null, null, null, false);
        }

        // 获取类级注解
        RequiresPermission classPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);

        // 获取方法级注解
        RequiresPermission methodPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);

        // 如果方法有注解，优先使用方法级
        if (methodPermission != null) {
            PermissionValidator.validate(
                    methodPermission,
                    handlerMethod.getBeanType().getName(),
                    handlerMethod.getMethod().getName());

            return new PermissionInfo(methodPermission.value(), methodPermission.name(), methodPermission.access(),
                    true);
        }

        // 如果类有注解，使用类级
        if (classPermission != null) {
            PermissionValidator.validate(classPermission, handlerMethod.getBeanType().getName(), "class-level");

            return new PermissionInfo(classPermission.value(), classPermission.name(), classPermission.access(), true);
        }

        // 无权限注解
        return new PermissionInfo(null, null, null, false);
    }

    /**
     * 检查是否有权限注解（类级或方法级）
     *
     * @param handlerMethod
     *            Spring HandlerMethod
     * @return true 如果有权限注解
     */
    public static boolean hasPermission(HandlerMethod handlerMethod) {
        if (handlerMethod == null) {
            return false;
        }

        return handlerMethod.getMethodAnnotation(RequiresPermission.class) != null
                || handlerMethod.getBeanType().getAnnotation(RequiresPermission.class) != null;
    }
}
