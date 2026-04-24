package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationCodeTemplateTest {

    private final MessageTemplateRegistry registry = new MessageTemplateRegistry();
    private final EmailVerificationCodeTemplate template = new EmailVerificationCodeTemplate(registry);

    @Test
    @DisplayName("LOGIN scene should render correct HTML")
    void loginScene_shouldRenderCorrectHtml() {
        String html = template.buildHtml(VerificationCodeScene.LOGIN, "123456");

        assertNotNull(html);
        assertTrue(html.contains("蓝网登录"));
        assertTrue(html.contains("您的验证码为："));
        assertTrue(html.contains("123456"));
        assertTrue(html.contains("验证码5分钟内有效。"));
    }

    @Test
    @DisplayName("RESET_PASSWORD scene should render correct HTML")
    void resetPasswordScene_shouldRenderCorrectHtml() {
        String html = template.buildHtml(VerificationCodeScene.RESET_PASSWORD, "654321");

        assertTrue(html.contains("蓝网密码重置"));
        assertTrue(html.contains("您正在重置密码，验证码为："));
        assertTrue(html.contains("654321"));
        assertTrue(html.contains("验证码5分钟内有效，如非本人操作请忽略此邮件"));
    }

    @Test
    @DisplayName("CHANGE_EMAIL_ORIGINAL scene should render correct HTML")
    void changeEmailOriginalScene_shouldRenderCorrectHtml() {
        String html = template.buildHtml(VerificationCodeScene.CHANGE_EMAIL_ORIGINAL, "111111");

        assertTrue(html.contains("蓝网修改邮箱 - 验证原邮箱"));
        assertTrue(html.contains("111111"));
    }

    @Test
    @DisplayName("CHANGE_EMAIL_NEW scene should render correct HTML")
    void changeEmailNewScene_shouldRenderCorrectHtml() {
        String html = template.buildHtml(VerificationCodeScene.CHANGE_EMAIL_NEW, "222222");

        assertTrue(html.contains("蓝网修改邮箱 - 验证新邮箱"));
        assertTrue(html.contains("222222"));
    }

    @Test
    @DisplayName("Null code should be handled gracefully")
    void nullCode_shouldRenderEmptyString() {
        String html = template.buildHtml(VerificationCodeScene.LOGIN, null);

        assertTrue(html.contains("蓝网登录"));
        // The code placeholder should be replaced with empty string, not remain as
        // {{code}}
        assertFalse(html.contains("{{code}}"));
    }

    @Test
    @DisplayName("All scenes should use consistent HTML structure")
    void allScenes_shouldUseConsistentStructure() {
        for (VerificationCodeScene scene : VerificationCodeScene.values()) {
            String html = template.buildHtml(scene, "000000");
            assertTrue(html.contains("<div"), scene.name() + " should contain div wrapper");
            assertTrue(html.contains("<h2"), scene.name() + " should contain h2 title");
            assertTrue(html.contains("000000"), scene.name() + " should contain the code");
        }
    }
}
