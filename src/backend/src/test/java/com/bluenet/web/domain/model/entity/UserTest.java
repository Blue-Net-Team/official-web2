package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User 领域实体单元测试。
 * <p>
 * 验证实体状态变更方法的边界行为，不依赖 Spring 容器。
 * </p>
 */
@DisplayName("User 领域实体测试")
class UserTest {

    private static User newUser() {
        return User.create(
                "2024001001",
                "test@example.com",
                1L,
                "encodedPassword",
                "测试用户",
                "昵称",
                1L,
                "计算机科学与技术",
                2024,
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "开发",
                null,
                null,
                null,
                null,
                "REFCODE1",
                "bio");
    }

    @Test
    @DisplayName("changePassword: 应更新密码")
    void changePassword_shouldUpdatePassword() {
        User user = newUser();
        user.changePassword("newEncodedPassword");
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    @DisplayName("changePassword: 空密码应抛异常")
    void changePassword_withBlankPassword_shouldThrow() {
        User user = newUser();
        assertThrows(IllegalArgumentException.class, () -> user.changePassword("  "));
        assertThrows(IllegalArgumentException.class, () -> user.changePassword(null));
    }

    @Test
    @DisplayName("updateAvatar: 应更新头像ID")
    void updateAvatar_shouldUpdateAvatarId() {
        User user = newUser();
        user.updateAvatar(100L);
        assertEquals(100L, user.getAvatarId());
    }

    @Test
    @DisplayName("updateAvatar: null 头像ID应抛异常")
    void updateAvatar_withNull_shouldThrow() {
        User user = newUser();
        assertThrows(IllegalArgumentException.class, () -> user.updateAvatar(null));
    }

    @Test
    @DisplayName("updateQrcodeId: 应允许设置为 null")
    void updateQrcodeId_shouldAllowNull() {
        User user = newUser();
        user.updateQrcodeId(200L);
        assertEquals(200L, user.getQrcodeId());
        user.updateQrcodeId(null);
        assertNull(user.getQrcodeId());
    }

    @Test
    @DisplayName("updateProfile: 仅更新非 null 字段")
    void updateProfile_shouldUpdateNonNullFieldsOnly() {
        User user = newUser();
        user.updateProfile("新姓名", null, null, null, Direction.STRUCTURAL_DESIGN, null, "新简介", 300L);

        assertEquals("新姓名", user.getUsername());
        assertEquals("昵称", user.getNickname()); // 未更新
        assertEquals(1L, user.getCollegeId()); // 未更新
        assertEquals("计算机科学与技术", user.getMajor()); // 未更新
        assertEquals(Direction.STRUCTURAL_DESIGN, user.getDirection());
        assertEquals(Gender.MALE, user.getGender()); // 未更新
        assertEquals("新简介", user.getBio());
        assertEquals(300L, user.getQrcodeId());
    }

    @Test
    @DisplayName("updateProfile: 应更新学院标识")
    void updateProfile_shouldUpdateCollegeId() {
        User user = newUser();
        user.updateProfile(null, null, 5L, null, null, null, null, null);
        assertEquals(5L, user.getCollegeId());
    }

    @Test
    @DisplayName("changeEmail: 应更新邮箱")
    void changeEmail_shouldUpdateEmail() {
        User user = newUser();
        user.changeEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    @DisplayName("changeEmail: 空邮箱应抛异常")
    void changeEmail_withBlankEmail_shouldThrow() {
        User user = newUser();
        assertThrows(IllegalArgumentException.class, () -> user.changeEmail("   "));
        assertThrows(IllegalArgumentException.class, () -> user.changeEmail(null));
    }

    @Test
    @DisplayName("resetPassword: 两次密码一致时更新密码")
    void resetPassword_withMatchedPasswords_shouldUpdatePassword() {
        User user = newUser();
        user.resetPassword("newPwd", "newPwd");
        assertEquals("newPwd", user.getPassword());
    }

    @Test
    @DisplayName("resetPassword: 两次密码不一致或为空时抛异常")
    void resetPassword_withMismatchedPasswords_shouldThrow() {
        User user = newUser();
        assertThrows(IllegalArgumentException.class, () -> user.resetPassword("newPwd", "diffPwd"));
        assertThrows(IllegalArgumentException.class, () -> user.resetPassword("  ", "  "));
    }

    @Test
    @DisplayName("updateAdminFields: 仅更新非 null 字段")
    void updateAdminFields_shouldUpdateNonNullFieldsOnly() {
        User user = newUser();
        user.updateAdminFields(
                2L,
                Direction.EMBEDDED,
                true,
                "算法",
                null,
                null,
                "管理员改名",
                null,
                null,
                null,
                Gender.FEMALE,
                null);

        assertEquals(2L, user.getRoleId());
        assertEquals(Direction.EMBEDDED, user.getDirection());
        assertEquals(true, user.getDisable());
        assertEquals("算法", user.getJob());
        assertEquals("2024001001", user.getStudentId()); // 未更新
        assertEquals("管理员改名", user.getUsername()); // 更新
        assertEquals("昵称", user.getNickname()); // 未更新
        assertEquals(Gender.FEMALE, user.getGender());
        assertEquals(2024, user.getAssessmentGradeYear()); // 未更新
    }

    @Test
    @DisplayName("reconstruct: 从数据库重建应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        User user = User.reconstruct(
                99L,
                "2024999999",
                "reconstruct@example.com",
                3L,
                "pwd",
                "重建用户",
                "重建昵称",
                5L,
                "专业",
                2025,
                Direction.EMBEDDED,
                Gender.FEMALE,
                "岗位",
                10L,
                true,
                20L,
                "github-1",
                "ghuser",
                "RC1234",
                "重建简介");

        assertEquals(99L, user.getId());
        assertEquals("2024999999", user.getStudentId());
        assertEquals("reconstruct@example.com", user.getEmail());
        assertEquals(3L, user.getRoleId());
        assertTrue(user.getDisable());
        assertEquals(20L, user.getQrcodeId());
    }
}
