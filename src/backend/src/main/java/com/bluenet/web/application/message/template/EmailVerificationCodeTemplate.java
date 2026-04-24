package com.bluenet.web.application.message.template;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通用邮箱验证码邮件模板。
 * <p>
 * 支持多种验证码场景（登录、密码重置、修改邮箱等），通过 {@link VerificationCodeScene} 枚举配置不同场景的显示文案。模板内容从
 * {@link MessageTemplateRegistry} 读取，支持管理后台动态覆盖。
 * </p>
 */
@Component
public class EmailVerificationCodeTemplate {

    private static final String CODE = "EMAIL_VERIFICATION_CODE";

    private final MessageTemplateRegistry registry;

    public EmailVerificationCodeTemplate(MessageTemplateRegistry registry) {
        this.registry = registry;
    }

    /**
     * 构建验证码 HTML 邮件内容。
     *
     * @param scene
     *            验证码场景。
     * @param code
     *            验证码。
     * @return HTML 邮件内容。
     */
    public String buildHtml(VerificationCodeScene scene, String code) {
        String template = registry.getTemplateContent(CODE);
        Map<String, String> variables = Map.of(
                "title",
                scene.getTitle(),
                "description",
                scene.getDescription(),
                "code",
                code != null ? code : "",
                "footer",
                scene.getFooter());
        return TemplateVariableSubstitutor.substitute(template, variables);
    }
}
