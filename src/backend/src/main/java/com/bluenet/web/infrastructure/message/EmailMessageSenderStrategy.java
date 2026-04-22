package com.bluenet.web.infrastructure.message;

import com.bluenet.web.application.message.MessageChannel;
import com.bluenet.web.application.message.MessageContentType;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.infrastructure.email.EmailSendException;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 邮件通道发送策略。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageSenderStrategy implements MessageSenderStrategy {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:}")
    private String fromAddress;

    @Override
    public MessageChannel channel() {
        return MessageChannel.EMAIL;
    }

    @Override
    public void send(MessageRequest request) {
        validateRecipient(request.recipient());
        // 未显式指定内容格式时按纯文本处理，避免调用方遗漏导致 NPE。
        MessageContentType contentType = request.contentType() == null
                ? MessageContentType.TEXT
                : request.contentType();
        String content = resolveContent(request, contentType);
        boolean html = contentType != MessageContentType.TEXT;
        log.info("Sending {} email to: {}", contentType, request.recipient());
        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
                helper.setTo(request.recipient());
                helper.setSubject(request.subject());
                helper.setText(content, html);
                if (fromAddress != null && !fromAddress.isBlank()) {
                    helper.setFrom(fromAddress);
                }
            });
            log.info("{} email sent successfully to: {}", contentType, request.recipient());
        } catch (Exception e) {
            log.error("Failed to send {} email to: {}", contentType, request.recipient(), e);
            throw new EmailSendException("Failed to send email to: " + request.recipient(), e);
        }
    }

    private String resolveContent(MessageRequest request, MessageContentType contentType) {
        // 模板变量替换属于具体通道策略的实现细节，不暴露给 application 层。
        if (contentType == MessageContentType.TEMPLATE) {
            return TemplateVariableSubstitutor.substitute(request.content(), request.variables());
        }
        return request.content();
    }

    private void validateRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient cannot be null or empty");
        }
    }
}
