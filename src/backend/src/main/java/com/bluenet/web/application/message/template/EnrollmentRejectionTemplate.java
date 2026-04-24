package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报名审核拒绝通知邮件模板。
 * <p>
 * 模板内容从 {@link MessageTemplateRegistry} 读取，支持管理后台动态覆盖。
 * </p>
 */
@Component
public class EnrollmentRejectionTemplate {

    private static final String CODE = "ENROLL_REJECTION";

    private final MessageTemplateRegistry registry;

    public EnrollmentRejectionTemplate(MessageTemplateRegistry registry) {
        this.registry = registry;
    }

    /**
     * 构建拒绝通知 HTML 邮件内容。
     *
     * @param username
     *            用户名。
     * @param rejectReason
     *            拒绝原因。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String username, String rejectReason) {
        String template = registry.getTemplateContent(CODE);
        Map<String, String> variables = Map.of(
                "username",
                username != null ? username : "",
                "rejectReason",
                rejectReason != null ? rejectReason : "");
        return TemplateVariableSubstitutor.substitute(template, variables);
    }
}
