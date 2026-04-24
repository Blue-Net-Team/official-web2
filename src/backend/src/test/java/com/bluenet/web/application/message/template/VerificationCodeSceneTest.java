package com.bluenet.web.application.message.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCodeSceneTest {

    @Test
    @DisplayName("LOGIN scene should have correct display texts")
    void loginScene_shouldHaveCorrectTexts() {
        assertEquals("登录", VerificationCodeScene.LOGIN.getTitle());
        assertEquals("您的验证码为：", VerificationCodeScene.LOGIN.getDescription());
        assertEquals("验证码5分钟内有效。", VerificationCodeScene.LOGIN.getFooter());
    }

    @Test
    @DisplayName("RESET_PASSWORD scene should have correct display texts")
    void resetPasswordScene_shouldHaveCorrectTexts() {
        assertEquals("密码重置", VerificationCodeScene.RESET_PASSWORD.getTitle());
        assertEquals("您正在重置密码，验证码为：", VerificationCodeScene.RESET_PASSWORD.getDescription());
        assertEquals("验证码5分钟内有效，如非本人操作请忽略此邮件", VerificationCodeScene.RESET_PASSWORD.getFooter());
    }

    @Test
    @DisplayName("CHANGE_EMAIL_ORIGINAL scene should have correct display texts")
    void changeEmailOriginalScene_shouldHaveCorrectTexts() {
        assertEquals("修改邮箱 - 验证原邮箱", VerificationCodeScene.CHANGE_EMAIL_ORIGINAL.getTitle());
        assertEquals("您的验证码为：", VerificationCodeScene.CHANGE_EMAIL_ORIGINAL.getDescription());
        assertEquals("验证码5分钟内有效，请勿泄露给他人。", VerificationCodeScene.CHANGE_EMAIL_ORIGINAL.getFooter());
    }

    @Test
    @DisplayName("CHANGE_EMAIL_NEW scene should have correct display texts")
    void changeEmailNewScene_shouldHaveCorrectTexts() {
        assertEquals("修改邮箱 - 验证新邮箱", VerificationCodeScene.CHANGE_EMAIL_NEW.getTitle());
        assertEquals("您的验证码为：", VerificationCodeScene.CHANGE_EMAIL_NEW.getDescription());
        assertEquals("验证码5分钟内有效，请勿泄露给他人。", VerificationCodeScene.CHANGE_EMAIL_NEW.getFooter());
    }

    @Test
    @DisplayName("All scenes should have non-null and non-empty properties")
    void allScenes_shouldHaveNonNullProperties() {
        for (VerificationCodeScene scene : VerificationCodeScene.values()) {
            assertNotNull(scene.getTitle(), scene.name() + " title should not be null");
            assertNotNull(scene.getDescription(), scene.name() + " description should not be null");
            assertNotNull(scene.getFooter(), scene.name() + " footer should not be null");
            assertFalse(scene.getTitle().isEmpty(), scene.name() + " title should not be empty");
            assertFalse(scene.getDescription().isEmpty(), scene.name() + " description should not be empty");
            assertFalse(scene.getFooter().isEmpty(), scene.name() + " footer should not be empty");
        }
    }
}
