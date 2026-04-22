package com.bluenet.web.infrastructure.email;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮件发送集成测试
 *
 * <p>
 * 此测试用于验证邮件发送功能是否正常工作。 测试需要配置以下环境变量：
 * <ul>
 * <li>MAIL_USERNAME - SMTP 发件人邮箱地址</li>
 * <li>MAIL_PASSWORD - SMTP 授权密码</li>
 * <li>TEST_EMAIL_TO - 测试收件人邮箱地址</li>
 * </ul>
 *
 * <p>
 * 如果未配置 TEST_EMAIL_TO 环境变量，测试将被跳过。
 *
 * <p>
 * 运行测试示例（PowerShell）：
 *
 * <pre>
 * $env:MAIL_USERNAME="your_email@163.com"
 * $env:MAIL_PASSWORD="your_auth_code"
 * $env:TEST_EMAIL_TO="recipient@example.com"
 * ./mvnw test -Dtest=EmailSenderIntegrationTest
 * </pre>
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class EmailSenderIntegrationTest extends BaseIntegrationTest {

    private static final String TEST_EMAIL_TO = System.getenv("TEST_EMAIL_TO");

    @Autowired
    private MessageDispatcher messageDispatcher;

    @Test
    @DisplayName("发送 Hello World 纯文本邮件")
    @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
    void sendHelloWorldEmail() {
        // Given
        String to = TEST_EMAIL_TO;
        String subject = "[蓝网科技] Hello World 测试邮件";
        String content = "Hello World!\n\n这是一封来自蓝网科技创新团队的测试邮件。\n\n如果您收到此邮件，说明邮件发送功能正常工作。";

        // When & Then
        assertDoesNotThrow(() -> {
            messageDispatcher.dispatch(MessageRequest.text(MessageChannel.EMAIL, to, subject, content));
            log.info("Hello World 邮件发送成功，收件人: {}", to);
        }, "邮件发送应该成功，不抛出异常");
    }

    @Test
    @DisplayName("发送 HTML 格式邮件")
    @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
    void sendHtmlEmail() {
        // Given
        String to = TEST_EMAIL_TO;
        String subject = "[蓝网科技] HTML 格式测试邮件";
        String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1 style="color: #2196F3;">Hello World!</h1>
                    <p>这是一封 <strong>HTML 格式</strong> 的测试邮件。</p>
                    <p>来自 <span style="color: #4CAF50;">蓝网科技创新团队</span></p>
                </body>
                </html>
                """;

        // When & Then
        assertDoesNotThrow(() -> {
            messageDispatcher.dispatch(MessageRequest.html(MessageChannel.EMAIL, to, subject, htmlContent));
            log.info("HTML 邮件发送成功，收件人: {}", to);
        }, "HTML 邮件发送应该成功，不抛出异常");
    }

    @Test
    @DisplayName("发送模板邮件")
    @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
    void sendTemplateEmail() {
        // Given
        String to = TEST_EMAIL_TO;
        String subject = "[蓝网科技] 模板邮件测试";
        String template = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>尊敬的 {{username}}，您好！</h2>
                    <p>您的验证码是：<strong style="color: #FF5722; font-size: 24px;">{{code}}</strong></p>
                    <p>验证码有效期为 {{expireTime}} 分钟，请尽快使用。</p>
                    <hr/>
                    <p style="color: #666; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                </body>
                </html>
                """;
        Map<String, String> variables = Map.of(
                "username",
                "测试用户",
                "code",
                "123456",
                "expireTime",
                "5");

        // When & Then
        assertDoesNotThrow(() -> {
            messageDispatcher.dispatch(MessageRequest.template(MessageChannel.EMAIL, to, subject, template, variables));
            log.info("模板邮件发送成功，收件人: {}", to);
        }, "模板邮件发送应该成功，不抛出异常");
    }

    @Test
    @DisplayName("发送异步邮件")
    @EnabledIfEnvironmentVariable(named = "TEST_EMAIL_TO", matches = ".+@.+\\..+")
    void sendAsyncEmail() throws InterruptedException {
        // Given
        String to = TEST_EMAIL_TO;
        String subject = "[蓝网科技] 异步邮件测试";
        String content = "这是一封异步发送的测试邮件。\n\nHello World from async!";

        // When & Then
        assertDoesNotThrow(() -> {
            messageDispatcher.dispatchAsync(MessageRequest.text(MessageChannel.EMAIL, to, subject, content));
            log.info("异步邮件已提交发送队列，收件人: {}", to);
        }, "异步邮件发送应该成功提交，不抛出异常");

        // 等待异步发送完成
        Thread.sleep(2000);
    }

    @Test
    @DisplayName("收件人地址为空时应抛出 IllegalArgumentException")
    void send_shouldThrowWhenRecipientIsNull() {
        // Given
        String to = null;
        String subject = "Test Subject";
        String content = "Test Content";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageDispatcher.dispatch(MessageRequest.text(MessageChannel.EMAIL, to, subject, content)));

        assertTrue(exception.getMessage().contains("Recipient cannot be null or empty"));
        log.info("正确抛出异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("收件人地址为空白字符串时应抛出 IllegalArgumentException")
    void send_shouldThrowWhenRecipientIsBlank() {
        // Given
        String to = "   ";
        String subject = "Test Subject";
        String content = "Test Content";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageDispatcher.dispatch(MessageRequest.text(MessageChannel.EMAIL, to, subject, content)));

        assertTrue(exception.getMessage().contains("Recipient cannot be null or empty"));
        log.info("正确抛出异常: {}", exception.getMessage());
    }
}
