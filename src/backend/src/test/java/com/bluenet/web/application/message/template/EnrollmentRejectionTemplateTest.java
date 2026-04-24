package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentRejectionTemplateTest {

    private final MessageTemplateRegistry registry = new MessageTemplateRegistry();
    private final EnrollmentRejectionTemplate template = new EnrollmentRejectionTemplate(registry);

    @Test
    @DisplayName("Should render rejection email with username and reason")
    void shouldRenderRejectionEmail() {
        String html = template.buildHtml("张三", "人数已满");

        assertNotNull(html);
        assertTrue(html.contains("张三"));
        assertTrue(html.contains("人数已满"));
        assertTrue(html.contains("报名申请未通过"));
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void shouldHandleNullUsername() {
        String html = template.buildHtml(null, "不符合要求");

        assertNotNull(html);
        assertTrue(html.contains("不符合要求"));
        // username placeholder should be replaced with empty string
        assertFalse(html.contains("{{username}}"));
    }

    @Test
    @DisplayName("Should handle null reason gracefully")
    void shouldHandleNullReason() {
        String html = template.buildHtml("李四", null);

        assertNotNull(html);
        assertTrue(html.contains("李四"));
        // reason placeholder should be replaced with empty string
        assertFalse(html.contains("{{rejectReason}}"));
    }
}
