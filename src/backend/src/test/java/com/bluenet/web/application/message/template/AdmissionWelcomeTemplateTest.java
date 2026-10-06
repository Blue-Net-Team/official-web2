package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.repository.mapper.MessageTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AdmissionWelcomeTemplateTest {

    private final MessageTemplateMapper mockMapper = mock(MessageTemplateMapper.class);
    private final MessageTemplateRegistry registry = new MessageTemplateRegistry(mockMapper);
    private final AdmissionWelcomeTemplate template = new AdmissionWelcomeTemplate(registry);

    @Test
    @DisplayName("Should render admission welcome email with all variables")
    void shouldRenderWelcomeEmail() {
        String html = template.buildHtml("张三", "全局", 0);

        assertNotNull(html);
        assertTrue(html.contains("张三"));
        assertTrue(html.contains("全局"));
        assertTrue(html.contains("第0轮"));
        assertTrue(html.contains("欢迎加入蓝网团队"));
        assertTrue(html.contains("Blue-Net-Team"));
        assertTrue(html.contains("GitHub"));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        String html = template.buildHtml(null, null, 0);

        assertNotNull(html);
        assertFalse(html.contains("{{nickname}}"));
        assertFalse(html.contains("{{directionLabel}}"));
        assertFalse(html.contains("{{epoch}}"));
    }

    @Test
    @DisplayName("Should return template subject")
    void shouldReturnSubject() {
        String subject = template.getSubject();

        assertNotNull(subject);
        assertTrue(subject.contains("欢迎加入蓝网团队"));
    }
}
