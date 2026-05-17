package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void changePassword_shouldUpdatePassword() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        user.changePassword("encoded-new");

        assertEquals("encoded-new", user.getPassword());
    }

    @Test
    void changePassword_withBlankPassword_shouldThrow() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThrows(IllegalArgumentException.class, () -> user.changePassword(" "));
    }

    // ========== Admin reset password ==========

    @Test
    void resetPassword_shouldUpdatePasswordWhenConfirmed() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        user.resetPassword("newPass123", "newPass123");

        assertEquals("newPass123", user.getPassword());
    }

    @Test
    void resetPassword_withMismatch_shouldThrow() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> user.resetPassword("abc123", "def456"));
        assertEquals("两次输入的密码不一致", exception.getMessage());
    }

    @Test
    void resetPassword_withBlankPassword_shouldThrow() {
        User user = User.reconstruct(
                1L,
                null,
                null,
                null,
                "old",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> user.resetPassword("", ""));
        assertEquals("密码不能为空", exception.getMessage());
    }

    // ========== Admin update fields ==========

    @Test
    void updateAdminFields_shouldUpdateAllowedFields() {
        User user = User.reconstruct(
                1L,
                "2024001001",
                "test@test.com",
                1L,
                "pass",
                "张三",
                "昵称",
                1L,
                "软件工程",
                null,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                null,
                false,
                null,
                null,
                null,
                null,
                "简介");

        user.updateAdminFields(2L, Direction.EMBEDDED, true, "测试工程师", null, null, null, null, null, null, null, null);

        assertEquals(2L, user.getRoleId());
        assertEquals(Direction.EMBEDDED, user.getDirection());
        assertTrue(user.getDisable());
        assertEquals("测试工程师", user.getJob());
    }

    @Test
    void updateAdminFields_withNullValues_shouldKeepExisting() {
        User user = User.reconstruct(
                1L,
                "2024001001",
                "test@test.com",
                1L,
                "pass",
                "张三",
                "昵称",
                1L,
                "软件工程",
                null,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                null,
                false,
                null,
                null,
                null,
                null,
                "简介");

        user.updateAdminFields(null, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(1L, user.getRoleId());
        assertEquals(Direction.COMPUTER_VISION, user.getDirection());
        assertFalse(user.getDisable());
        assertEquals("开发", user.getJob());
    }
}
