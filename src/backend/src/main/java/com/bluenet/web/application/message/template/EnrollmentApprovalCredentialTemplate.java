package com.bluenet.web.application.message.template;

import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报名审核通过初始凭据邮件模板。
 */
@Component
public class EnrollmentApprovalCredentialTemplate {

    private static final String TEMPLATE = """
            <div style="font-family:Arial,sans-serif;max-width:640px;margin:0 auto;padding:20px;color:#333;">
                <h2 style="color:#1f7ae0;text-align:center;">蓝网报名审核已通过</h2>
                <p>您好，{{username}} 同学：</p>
                <p>您的报名申请（学号：<strong>{{studentId}}</strong>）已审核通过，系统已为您创建账号。</p>
                <div style="background:#f6f8fb;border:1px solid #e6ebf2;border-radius:8px;padding:16px;margin:16px 0;">
                    <p style="margin:0 0 8px 0;">初始登录密码：</p>
                    <p style="margin:0;font-size:22px;font-weight:bold;color:#d4380d;letter-spacing:1px;">{{initialPassword}}</p>
                </div>
                <p style="margin:0 0 8px 0;">安全提示：</p>
                <ul style="margin-top:0;padding-left:20px;">
                    <li>请在首次登录后尽快修改密码。</li>
                    <li>请勿将密码透露给他人。</li>
                </ul>
                <p style="color:#999;font-size:12px;">此邮件由系统自动发送，请勿直接回复。</p>
            </div>
            """;

    /**
     * 构建报名审核通过 HTML 邮件内容。
     *
     * @param username
     *            学生姓名。
     * @param studentId
     *            学号。
     * @param initialPassword
     *            初始登录密码。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String username, String studentId, String initialPassword) {
        Map<String, String> variables = Map.of(
                "username",
                username != null ? username : "",
                "studentId",
                studentId != null ? studentId : "",
                "initialPassword",
                initialPassword != null ? initialPassword : "");
        return TemplateVariableSubstitutor.substitute(TEMPLATE, variables);
    }
}
