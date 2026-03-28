package com.bluenet.web.infrastructure.security.aspect;

import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.infrastructure.security.RoleType;
import com.bluenet.web.infrastructure.security.util.RoleHierarchy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * 权限切面 拦截所有带@RequiresPermission注解的方法，执行权限校验
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    private final PermissionCache permissionCache;

    public PermissionAspect(PermissionCache permissionCache) {
        this.permissionCache = permissionCache;
    }

    /**
     * 拦截所有带@Permission注解的方法
     */
    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint pjp, RequiresPermission requiresPermission) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            logger.warn("Cannot get current request, denying access");
            throw new Forbidden("Access denied");
        }

        // 放行超管
        boolean superAdmin = isSuperAdmin();
        logger.debug("Permission check: isSuperAdmin={}, permission={}", superAdmin, requiresPermission.value());
        if (superAdmin) {
            return pjp.proceed();
        }

        // 根据访问级别处理
        switch (requiresPermission.access()) {
            case PUBLIC :
                // 公开访问，直接放行
                return pjp.proceed();

            case AUTHENTICATED :
                // 需要登录
                if (!isAuthenticated()) {
                    logger.warn("Unauthenticated access to AUTHENTICATED endpoint: {}", requiresPermission.value());
                    throw new Unauthorized("未认证");
                }
                return pjp.proceed();

            case PROTECTED :
            default :
                // 需要权限校验
                if (!isAuthenticated()) {
                    logger.warn("Unauthenticated access to PROTECTED endpoint: {}", requiresPermission.value());
                    throw new Unauthorized("未认证");
                }

                if (!hasPermission(requiresPermission.value())) {
                    logger.warn("Access denied to {} for user", requiresPermission.value());
                    throw new Forbidden("无权限");
                }

                return pjp.proceed();
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 检查是否已认证
     */
    private boolean isAuthenticated() {
        return UserCTX.isAuthenticated();
    }

    /**
     * 检查是否有权限
     */
    private boolean hasPermission(String permissionValue) {
        // 取出用户
        UserVO user = UserCTX.getCurrentUser();

        // 取出权限
        Set<String> userPermissions = user.getPermissions();

        // 权限验证
        return userPermissions.contains(permissionValue);
    }

    /**
     * 检查是否是超级管理员
     */
    private boolean isSuperAdmin() {
        UserVO user = UserCTX.getCurrentUser();
        if (user == null || user.getRoleName() == null) {
            logger.debug("isSuperAdmin: user or roleName is null");
            return false;
        }
        String roleName = user.getRoleName().trim().toUpperCase();
        RoleType roleType = RoleType.fromName(roleName);
        if (roleType == null) {
            logger.debug("isSuperAdmin: RoleType.fromName returned null for roleName: {}", roleName);
            return false;
        }
        boolean isSuperAdmin = RoleHierarchy.isSuperAdmin(roleType);
        logger.debug(
                "isSuperAdmin: user roleName={}, normalized={}, roleType={}, isSuperAdmin={}",
                user.getRoleName(),
                roleName,
                roleType,
                isSuperAdmin);
        return isSuperAdmin;
    }
}
