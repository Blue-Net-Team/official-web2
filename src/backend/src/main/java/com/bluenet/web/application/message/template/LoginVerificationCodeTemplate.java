package com.bluenet.web.application.message.template;

import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 登录验证码邮件模板。
 */
@Component
public class LoginVerificationCodeTemplate {

    private static final String TEMPLATE = """
            <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                <h2 style="color:#fa8c16;text-align:center;">蓝网登录验证码</h2>
                <p style="text-align:center;font-size:14px;color:#666;">您的验证码为：</p>
                <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">{{code}}</p>
                <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效。</p>
            </div>
            """;

    /**
     * 构建登录验证码 HTML 邮件内容。
     *
     * @param code
     *            验证码。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String code) {
        Map<String, String> variables = Map.of("code", code != null ? code : "");
        return TemplateVariableSubstitutor.substitute(TEMPLATE, variables);
    }
}
