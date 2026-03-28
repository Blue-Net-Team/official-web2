package com.bluenet.web.infrastructure.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解 用于标记Controller方法的访问权限
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 权限唯一标识 格式: resource:action (如 user:create, assessment-time:view-self) 必须满足正则:
     * ^[a-z]+:[a-z]+$
     */
    String value();

    /**
     * 权限显示名称 用于后台管理和日志展示
     */
    String name() default "";

    /**
     * 访问级别 默认 PROTECTED（需要权限校验）
     */
    AccessLevel access() default AccessLevel.PROTECTED;
}
