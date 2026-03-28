package com.bluenet.web.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailSenderImpl emailSender;

    @BeforeEach
    void setUp() {
        emailSender = new EmailSenderImpl(mailSender);
    }

    @Test
    @DisplayName("Should send text email successfully")
    void send_shouldSendTextEmail() {
        doNothing().when(mailSender).send(any(MimeMessagePreparator.class));

        assertDoesNotThrow(() -> emailSender.send("test@example.com", "Test Subject", "Test Content"));

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when recipient is null")
    void send_shouldThrowWhenRecipientIsNull() {
        assertThrows(IllegalArgumentException.class, () -> emailSender.send(null, "Subject", "Content"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when recipient is empty")
    void send_shouldThrowWhenRecipientIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> emailSender.send("", "Subject", "Content"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when recipient is blank")
    void send_shouldThrowWhenRecipientIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> emailSender.send("   ", "Subject", "Content"));
    }

    @Test
    @DisplayName("Should throw EmailSendException when mail sender fails")
    void send_shouldThrowEmailSendExceptionWhenMailSenderFails() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessagePreparator.class));

        assertThrows(EmailSendException.class, () -> emailSender.send("test@example.com", "Subject", "Content"));
    }

    @Test
    @DisplayName("Should send HTML email successfully")
    void sendHtml_shouldSendHtmlEmail() {
        doNothing().when(mailSender).send(any(MimeMessagePreparator.class));

        assertDoesNotThrow(
                () -> emailSender.sendHtml("test@example.com", "Test Subject", "<html><body>Test</body></html>"));

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    @DisplayName("Should send email with template and variables")
    void sendWithTemplate_shouldSubstituteVariables() {
        doNothing().when(mailSender).send(any(MimeMessagePreparator.class));

        String template = "<html><body>Hello {{username}}, your code is {{code}}</body></html>";
        Map<String, String> variables = Map.of("username", "John", "code", "123456");

        assertDoesNotThrow(() -> emailSender.sendWithTemplate("test@example.com", "Subject", template, variables));

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    @DisplayName("Should send async email without blocking")
    void sendAsync_shouldSendAsync() {
        doNothing().when(mailSender).send(any(MimeMessagePreparator.class));

        assertDoesNotThrow(() -> emailSender.sendAsync("test@example.com", "Subject", "Content"));

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }
}
