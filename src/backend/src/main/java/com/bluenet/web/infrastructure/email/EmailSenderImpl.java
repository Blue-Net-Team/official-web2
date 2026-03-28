package com.bluenet.web.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:}")
    private String fromAddress;

    @Override
    public void send(String to, String subject, String textContent) {
        validateRecipient(to);
        log.info("Sending text email to: {}", to);
        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(textContent, false);
                if (fromAddress != null && !fromAddress.isBlank()) {
                    helper.setFrom(fromAddress);
                }
            });
            log.info("Text email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send text email to: {}", to, e);
            throw new EmailSendException("Failed to send email to: " + to, e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        validateRecipient(to);
        log.info("Sending HTML email to: {}", to);
        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                if (fromAddress != null && !fromAddress.isBlank()) {
                    helper.setFrom(fromAddress);
                }
            });
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new EmailSendException("Failed to send HTML email to: " + to, e);
        }
    }

    @Override
    public void sendWithTemplate(String to, String subject, String templateContent, Map<String, String> variables) {
        String processedContent = TemplateVariableSubstitutor.substitute(templateContent, variables);
        sendHtml(to, subject, processedContent);
    }

    @Override
    @Async
    public void sendAsync(String to, String subject, String textContent) {
        log.info("Sending text email asynchronously to: {}", to);
        send(to, subject, textContent);
    }

    @Override
    @Async
    public void sendHtmlAsync(String to, String subject, String htmlContent) {
        log.info("Sending HTML email asynchronously to: {}", to);
        sendHtml(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendWithTemplateAsync(String to, String subject, String templateContent,
            Map<String, String> variables) {
        log.info("Sending template email asynchronously to: {}", to);
        sendWithTemplate(to, subject, templateContent, variables);
    }

    private void validateRecipient(String to) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email address cannot be null or empty");
        }
    }
}
