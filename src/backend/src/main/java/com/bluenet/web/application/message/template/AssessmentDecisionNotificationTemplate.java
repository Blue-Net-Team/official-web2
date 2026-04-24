package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 考核结果通知模板，只负责内容生成，不承担消息发送职责。
 * <p>
 * 模板内容从 {@link MessageTemplateRegistry} 读取，支持管理后台动态覆盖。
 * </p>
 */
@Component
public class AssessmentDecisionNotificationTemplate {

    private static final String CODE = "ASSESSMENT_DECISION_NOTIFICATION";

    private final MessageTemplateRegistry registry;

    public AssessmentDecisionNotificationTemplate(MessageTemplateRegistry registry) {
        this.registry = registry;
    }

    /**
     * 构建决策通知 HTML 邮件内容。
     *
     * @param nickname
     *            考生昵称。
     * @param directionLabel
     *            考核方向名称。
     * @param epoch
     *            轮次。
     * @param resultText
     *            结果文本。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String nickname, String directionLabel, int epoch, String resultText) {
        String template = registry.getTemplateContent(CODE);
        String color = ("通过".equals(resultText) || "录取".equals(resultText)) ? "#52c41a" : "#ff4d4f";
        Map<String, String> variables = Map.of(
                "nickname",
                nickname != null ? nickname : "",
                "directionLabel",
                directionLabel != null ? directionLabel : "",
                "epoch",
                String.valueOf(epoch),
                "color",
                color,
                "resultText",
                resultText != null ? resultText : "");
        return TemplateVariableSubstitutor.substitute(template, variables);
    }
}
