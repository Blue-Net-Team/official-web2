package com.bluenet.web.infrastructure.security.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.PermissionValidator;

/**
 * PermissionValidator 单元测试。
 * <p>
 * 验证权限值格式校验与注解校验的通过/失败场景。
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    private static final String CLASS_NAME = "com.bluenet.web.TestController";
    private static final String METHOD_NAME = "testMethod";

    @Test
    @DisplayName("isValid 应接受符合 resource:action 格式的权限值")
    void isValid_shouldAcceptValidPermissionValues() {
        assertTrue(PermissionValidator.isValid("user:create"));
        assertTrue(PermissionValidator.isValid("assessment-time:view-self"));
        assertTrue(PermissionValidator.isValid("system:log:export"));
    }

    @Test
    @DisplayName("isValid 应拒绝包含大写或非法字符的权限值")
    void isValid_shouldRejectInvalidPermissionValues() {
        assertFalse(PermissionValidator.isValid("User:create"));
        assertFalse(PermissionValidator.isValid("user:Create"));
        assertFalse(PermissionValidator.isValid("user create"));
        assertFalse(PermissionValidator.isValid("user_create"));
        assertFalse(PermissionValidator.isValid("user"));
        assertFalse(PermissionValidator.isValid(":user"));
        assertFalse(PermissionValidator.isValid("user:"));
    }

    @Test
    @DisplayName("isValid 对 null 和空字符串应返回 false")
    void isValid_nullOrEmpty_shouldReturnFalse() {
        assertFalse(PermissionValidator.isValid(null));
        assertFalse(PermissionValidator.isValid(""));
        assertFalse(PermissionValidator.isValid("   "));
    }

    @Test
    @DisplayName("validate(String) 对有效权限值不应抛出异常")
    void validateString_valid_shouldNotThrow() {
        PermissionValidator.validate("user:update", CLASS_NAME, METHOD_NAME);
    }

    @Test
    @DisplayName("validate(String) 对空值应抛出 IllegalArgumentException")
    void validateString_emptyValue_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionValidator.validate("", CLASS_NAME, METHOD_NAME));

        assertTrue(exception.getMessage().contains("Permission value cannot be empty"));
        assertTrue(exception.getMessage().contains(CLASS_NAME));
        assertTrue(exception.getMessage().contains(METHOD_NAME));
    }

    @Test
    @DisplayName("validate(String) 对 null 值应抛出 IllegalArgumentException")
    void validateString_nullValue_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionValidator.validate((String) null, CLASS_NAME, METHOD_NAME));

        assertTrue(exception.getMessage().contains("Permission value cannot be empty"));
    }

    @Test
    @DisplayName("validate(String) 对格式错误权限值应抛出 IllegalArgumentException")
    void validateString_invalidFormat_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionValidator.validate("user-create", CLASS_NAME, METHOD_NAME));

        assertTrue(exception.getMessage().contains("Invalid permission value 'user-create'"));
        assertTrue(exception.getMessage().contains(CLASS_NAME));
        assertTrue(exception.getMessage().contains(METHOD_NAME));
    }

    @Test
    @DisplayName("validate(RequiresPermission) 对有效注解不应抛出异常")
    void validateAnnotation_valid_shouldNotThrow() {
        @RequiresPermission(value = "user:delete", name = "删除用户", access = AccessLevel.PROTECTED)
        class ValidController {
        }

        RequiresPermission annotation = ValidController.class.getAnnotation(RequiresPermission.class);
        PermissionValidator.validate(annotation, CLASS_NAME, METHOD_NAME);
    }

    @Test
    @DisplayName("validate(RequiresPermission) 对 null 注解应直接返回")
    void validateAnnotation_null_shouldNotThrow() {
        PermissionValidator.validate((RequiresPermission) null, CLASS_NAME, METHOD_NAME);
    }

    @Test
    @DisplayName("validate(RequiresPermission) 对空 value 应抛出 IllegalArgumentException")
    void validateAnnotation_emptyValue_shouldThrow() {
        @RequiresPermission(value = "", name = "空权限")
        class EmptyController {
        }

        RequiresPermission annotation = EmptyController.class.getAnnotation(RequiresPermission.class);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionValidator.validate(annotation, CLASS_NAME, METHOD_NAME));

        assertTrue(exception.getMessage().contains("Permission value cannot be empty"));
    }

    @Test
    @DisplayName("validate(RequiresPermission) 对无效 value 应抛出 IllegalArgumentException")
    void validateAnnotation_invalidValue_shouldThrow() {
        @RequiresPermission(value = "INVALID", name = "无效权限")
        class InvalidController {
        }

        RequiresPermission annotation = InvalidController.class.getAnnotation(RequiresPermission.class);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PermissionValidator.validate(annotation, CLASS_NAME, METHOD_NAME));

        assertTrue(exception.getMessage().contains("Invalid permission value 'INVALID'"));
    }
}
