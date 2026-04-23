package com.bluenet.web.application.message.template;

import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 修改邮箱验证码邮件模板。
 */
@Component
public class ChangeEmailVerificationCodeTemplate {

    private static final String TEMPLATE = """
            <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                <h2 style="color:#fa8c16;text-align:center;">蓝网修改邮箱 - {{action}}</h2>
                <p style="text-align:center;font-size:14px;color:#666;">您的验证码为：</p>
                <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">{{code}}</p>
                <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效，请勿泄露给他人。</p>
            </div>
            """;

    /**
     * 构建修改邮箱验证码 HTML 邮件内容。
     *
     * @param code
     *            验证码。
     * @param scene
     *            场景，支持 change-email-original 或 change-email-new。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String code, String scene) {
        String action = "change-email-original".equals(scene) ? "验证原邮箱" : "验证新邮箱";
        Map<String, String> variables = Map.of(
                "action",
                action,
                "code",
                code != null ? code : "");
        return TemplateVariableSubstitutor.substitute(TEMPLATE, variables);
    }
}
