package com.bluenet.web.infrastructure.email;

import java.util.Map;

public interface EmailSender {

    void send(String to, String subject, String textContent);

    void sendHtml(String to, String subject, String htmlContent);

    void sendWithTemplate(String to, String subject, String templateContent, Map<String, String> variables);

    void sendAsync(String to, String subject, String textContent);

    void sendHtmlAsync(String to, String subject, String htmlContent);

    void sendWithTemplateAsync(String to, String subject, String templateContent, Map<String, String> variables);
}
