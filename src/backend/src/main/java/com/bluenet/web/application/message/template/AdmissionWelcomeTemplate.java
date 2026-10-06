package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 录取欢迎邮件模板，只负责内容生成，不承担消息发送职责。
 * <p>
 * 模板内容从 {@link MessageTemplateRegistry} 读取，支持管理后台动态覆盖。
 * </p>
 */
@Component
public class AdmissionWelcomeTemplate {

    private static final String CODE = "ADMISSION_WELCOME";

    private final MessageTemplateRegistry registry;

    public AdmissionWelcomeTemplate(MessageTemplateRegistry registry) {
        this.registry = registry;
    }

    /**
     * 构建录取欢迎 HTML 邮件内容。
     *
     * @param nickname
     *            考生昵称。
     * @param directionLabel
     *            考核方向名称。
     * @param epoch
     *            轮次。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String nickname, String directionLabel, int epoch) {
        String template = registry.getTemplateContent(CODE);
        Map<String, String> variables = Map.of(
                "nickname",
                nickname != null ? nickname : "",
                "directionLabel",
                directionLabel != null ? directionLabel : "",
                "epoch",
                String.valueOf(epoch));
        return TemplateVariableSubstitutor.substitute(template, variables);
    }

    /**
     * 获取当前邮件主题（优先使用数据库覆盖值）。
     *
     * @return 邮件主题。
     */
    public String getSubject() {
        return registry.getTemplateSubject(CODE);
    }
}
