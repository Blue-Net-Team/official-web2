package com.bluenet.web.infrastructure.security.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.PermissionResolver;

/**
 * PermissionResolver 单元测试。
 * <p>
 * 验证类级与方法级权限注解的解析优先级及合法性校验。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PermissionResolverTest {

    @Test
    @DisplayName("resolve 对 null HandlerMethod 应返回无权限信息")
    void resolve_nullHandlerMethod_shouldReturnNoPermission() {
        PermissionResolver.PermissionInfo info = PermissionResolver.resolve(null);

        assertNotNull(info);
        assertFalse(info.hasPermission());
        assertEquals(null, info.getValue());
        assertEquals(null, info.getName());
        assertEquals(null, info.getAccess());
    }

    @Test
    @DisplayName("resolve 应优先使用方法级权限注解")
    void resolve_methodAnnotation_shouldTakePrecedence() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new ClassLevelController(),
                ClassLevelController.class.getMethod("methodLevelEndpoint"));

        PermissionResolver.PermissionInfo info = PermissionResolver.resolve(handlerMethod);

        assertTrue(info.hasPermission());
        assertEquals("method:action", info.getValue());
        assertEquals("方法级权限", info.getName());
        assertEquals(AccessLevel.AUTHENTICATED, info.getAccess());
    }

    @Test
    @DisplayName("resolve 仅类级注解时应返回类级权限信息")
    void resolve_classAnnotationOnly_shouldReturnClassLevelPermission() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new ClassLevelController(),
                ClassLevelController.class.getMethod("classLevelEndpoint"));

        PermissionResolver.PermissionInfo info = PermissionResolver.resolve(handlerMethod);

        assertTrue(info.hasPermission());
        assertEquals("class:action", info.getValue());
        assertEquals("类级权限", info.getName());
        assertEquals(AccessLevel.PROTECTED, info.getAccess());
    }

    @Test
    @DisplayName("resolve 对无注解 Controller 应返回无权限信息")
    void resolve_noAnnotation_shouldReturnNoPermission() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new NoAnnotationController(),
                NoAnnotationController.class.getMethod("publicEndpoint"));

        PermissionResolver.PermissionInfo info = PermissionResolver.resolve(handlerMethod);

        assertFalse(info.hasPermission());
        assertEquals(null, info.getValue());
    }

    @Test
    @DisplayName("resolve 应校验方法级权限值格式并抛出异常")
    void resolve_invalidMethodPermission_shouldThrowException() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new InvalidMethodController(),
                InvalidMethodController.class.getMethod("invalidEndpoint"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionResolver.resolve(handlerMethod));

        assertTrue(exception.getMessage().contains("Invalid permission value"));
    }

    @Test
    @DisplayName("resolve 应校验类级权限值格式并抛出异常")
    void resolve_invalidClassPermission_shouldThrowException() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new InvalidClassController(),
                InvalidClassController.class.getMethod("endpoint"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionResolver.resolve(handlerMethod));

        assertTrue(exception.getMessage().contains("Invalid permission value"));
    }

    @Test
    @DisplayName("hasPermission 应识别方法级注解")
    void hasPermission_methodAnnotation_shouldReturnTrue() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new ClassLevelController(),
                ClassLevelController.class.getMethod("methodLevelEndpoint"));

        assertTrue(PermissionResolver.hasPermission(handlerMethod));
    }

    @Test
    @DisplayName("hasPermission 应识别类级注解")
    void hasPermission_classAnnotation_shouldReturnTrue() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new ClassLevelController(),
                ClassLevelController.class.getMethod("classLevelEndpoint"));

        assertTrue(PermissionResolver.hasPermission(handlerMethod));
    }

    @Test
    @DisplayName("hasPermission 对无注解 Controller 应返回 false")
    void hasPermission_noAnnotation_shouldReturnFalse() throws NoSuchMethodException {
        HandlerMethod handlerMethod = new HandlerMethod(
                new NoAnnotationController(),
                NoAnnotationController.class.getMethod("publicEndpoint"));

        assertFalse(PermissionResolver.hasPermission(handlerMethod));
    }

    @Test
    @DisplayName("hasPermission 对 null HandlerMethod 应返回 false")
    void hasPermission_nullHandlerMethod_shouldReturnFalse() {
        assertFalse(PermissionResolver.hasPermission(null));
    }

    @RestController
    @RequestMapping("/api/v1/test/class")
    @RequiresPermission(value = "class:action", name = "类级权限", access = AccessLevel.PROTECTED)
    static class ClassLevelController {

        @GetMapping("/method")
        @RequiresPermission(value = "method:action", name = "方法级权限", access = AccessLevel.AUTHENTICATED)
        public String methodLevelEndpoint() {
            return "method";
        }

        @GetMapping("/class-only")
        public String classLevelEndpoint() {
            return "class";
        }
    }

    @RestController
    static class NoAnnotationController {

        @GetMapping("/public")
        public String publicEndpoint() {
            return "public";
        }
    }

    @RestController
    static class InvalidMethodController {

        @GetMapping("/invalid")
        @RequiresPermission(value = "INVALID_PERMISSION", name = "无效方法权限")
        public String invalidEndpoint() {
            return "invalid";
        }
    }

    @RestController
    @RequiresPermission(value = "ALSO_INVALID", name = "无效类权限")
    static class InvalidClassController {

        @GetMapping("/invalid")
        public String endpoint() {
            return "invalid";
        }
    }
}
