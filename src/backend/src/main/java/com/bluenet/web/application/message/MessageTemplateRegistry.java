package com.bluenet.web.application.message;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bluenet.web.infrastructure.email.TemplateVariableSubstitutor;
import com.bluenet.web.infrastructure.repository.dataobject.MessageTemplateDO;
import com.bluenet.web.infrastructure.repository.mapper.MessageTemplateMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 消息模板注册表。
 * <p>
 * 维护系统中所有消息模板的元数据、内容覆盖和主题覆盖。 模板元数据（编码、名称、描述、变量列表、默认内容）在内存中硬编码注册；运行时覆盖的 content
 * 和 subject 持久化到数据库，启动时自动加载。
 * </p>
 */
@Component
public class MessageTemplateRegistry {

    private final MessageTemplateMapper mapper;
    private final Map<String, TemplateEntry> templates = new LinkedHashMap<>();
    private final Map<String, String> contentOverrides = new HashMap<>();
    private final Map<String, String> subjectOverrides = new HashMap<>();

    @Autowired
    public MessageTemplateRegistry(MessageTemplateMapper mapper) {
        this.mapper = mapper;
        init();
    }

    /**
     * 无参构造，供不依赖数据库的单元测试使用。
     */
    public MessageTemplateRegistry() {
        this.mapper = null;
        init();
    }

    @PostConstruct
    void init() {
        if (!templates.isEmpty()) {
            return;
        }
        register(
                "EMAIL_VERIFICATION_CODE",
                "邮箱验证码",
                "蓝网验证码",
                "通用邮箱验证码模板，支持登录、密码重置、修改邮箱等场景",
                List.of("title", "description", "code", "footer"),
                """
                        <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                            <h2 style="color:#fa8c16;text-align:center;">蓝网{{title}}</h2>
                            <p style="text-align:center;font-size:14px;color:#666;">{{description}}</p>
                            <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">{{code}}</p>
                            <p style="text-align:center;font-size:12px;color:#999;">{{footer}}</p>
                        </div>
                        """);

        register(
                "ENROLL_APPROVAL_CREDENTIAL",
                "报名审核通过通知",
                "蓝网报名审核已通过",
                "报名审核通过后发送的账号初始密码通知",
                List.of("username", "studentId", "initialPassword"),
                """
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
                        """);

        register(
                "ENROLL_REJECTION",
                "报名审核拒绝通知",
                "蓝网报名审核未通过",
                "报名被拒绝时发送的通知",
                List.of("username", "rejectReason"),
                """
                        <div style="font-family:Arial,sans-serif;max-width:640px;margin:0 auto;padding:20px;color:#333;">
                            <h2 style="color:#ff4d4f;text-align:center;">蓝网报名申请未通过</h2>
                            <p>您好，{{username}} 同学：</p>
                            <p>很遗憾地通知您，您的报名申请未能通过审核。</p>
                            <div style="background:#fff2f0;border:1px solid #ffccc7;border-radius:8px;padding:16px;margin:16px 0;">
                                <p style="margin:0 0 8px 0;font-weight:bold;">拒绝原因：</p>
                                <p style="margin:0;color:#ff4d4f;">{{rejectReason}}</p>
                            </div>
                            <p>如果您对审核结果有疑问，可以联系相关方向管理员咨询。</p>
                            <p style="color:#999;font-size:12px;">此邮件由系统自动发送，请勿直接回复。</p>
                        </div>
                        """);

        register(
                "ASSESSMENT_DECISION_NOTIFICATION",
                "考核结果通知",
                "[蓝网] 考核结果通知",
                "每轮考核结束后发送的结果通知",
                List.of("nickname", "directionLabel", "epoch", "color", "resultText"),
                """
                        <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                          <h2 style="color: #1890ff;">考核结果通知</h2>
                          <p>{{nickname}} 你好，</p>
                          <p>你参加的 <strong>{{directionLabel}}方向第{{epoch}}轮</strong> 考核结果已公布：</p>
                          <p style="font-size: 18px; font-weight: bold; color: {{color}};">{{resultText}}</p>
                          <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                          <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                        </div>
                        """);

        if (mapper != null) {
            loadFromDatabase();
        }
    }

    private void loadFromDatabase() {
        List<MessageTemplateDO> records = mapper.selectList(null);
        for (MessageTemplateDO record : records) {
            String code = record.getCode();
            if (record.getContent() != null) {
                contentOverrides.put(code, record.getContent());
            }
            if (record.getSubject() != null) {
                subjectOverrides.put(code, record.getSubject());
            }
        }
    }

    private void register(String code, String name, String subject, String description,
            List<String> variables, String defaultContent) {
        templates.put(code, new TemplateEntry(code, name, subject, description, variables, defaultContent));
    }

    public List<MessageTemplateInfo> listAll() {
        return templates.values()
                .stream()
                .map(this::toInfo)
                .toList();
    }

    public Optional<MessageTemplateInfo> findByCode(String code) {
        return Optional.ofNullable(templates.get(code))
                .map(this::toInfo);
    }

    public void updateContent(String code, String newContent) {
        TemplateEntry entry = getEntryOrThrow(code);
        validateVariables(newContent, entry.variables);
        contentOverrides.put(code, newContent);
        if (mapper != null) {
            upsertToDb(code, entry, newContent, null);
        }
    }

    public void updateSubject(String code, String newSubject) {
        TemplateEntry entry = getEntryOrThrow(code);
        subjectOverrides.put(code, newSubject);
        if (mapper != null) {
            upsertToDb(code, entry, null, newSubject);
        }
    }

    public String preview(String code, Map<String, String> variables) {
        String content = getTemplateContent(code);
        return TemplateVariableSubstitutor.substitute(content, variables);
    }

    /**
     * 获取指定模板的当前内容（优先使用覆盖内容）。
     *
     * @param code
     *            模板编码
     * @return 模板内容字符串
     */
    public String getTemplateContent(String code) {
        TemplateEntry entry = getEntryOrThrow(code);
        return contentOverrides.getOrDefault(code, entry.defaultContent);
    }

    /**
     * 获取指定模板的当前主题（优先使用数据库覆盖值）。
     *
     * @param code
     *            模板编码
     * @return 邮件主题字符串
     */
    public String getTemplateSubject(String code) {
        TemplateEntry entry = getEntryOrThrow(code);
        return subjectOverrides.getOrDefault(code, entry.subject);
    }

    private TemplateEntry getEntryOrThrow(String code) {
        TemplateEntry entry = templates.get(code);
        if (entry == null) {
            throw new IllegalArgumentException("模板不存在: " + code);
        }
        return entry;
    }

    private void validateVariables(String content, List<String> expectedVariables) {
        // Simple validation: check if content contains unsupported {{xxx}} placeholders
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(\\w+)}}");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String var = matcher.group(1);
            if (!expectedVariables.contains(var)) {
                throw new IllegalArgumentException("模板内容包含不支持的变量: {{" + var + "}}，可用变量: " + expectedVariables);
            }
        }
    }

    private void upsertToDb(String code, TemplateEntry entry, String content, String subject) {
        MessageTemplateDO existing = mapper.selectOne(
                new QueryWrapper<MessageTemplateDO>().eq("code", code));

        if (existing != null) {
            if (content != null) {
                existing.setContent(content);
            }
            if (subject != null) {
                existing.setSubject(subject);
            }
            mapper.updateById(existing);
        } else {
            MessageTemplateDO record = new MessageTemplateDO();
            record.setCode(code);
            record.setName(entry.name);
            record.setSubject(subject != null ? subject : entry.subject);
            record.setContent(content);
            record.setDescription(entry.description);
            record.setEnabled(true);
            mapper.insert(record);
        }
    }

    private MessageTemplateInfo toInfo(TemplateEntry entry) {
        String content = contentOverrides.getOrDefault(entry.code, entry.defaultContent);
        String subject = subjectOverrides.getOrDefault(entry.code, entry.subject);
        return new MessageTemplateInfo(
                entry.code, entry.name, subject, entry.description,
                entry.variables, content, entry.defaultContent, true);
    }

    private record TemplateEntry(
            String code, String name, String subject, String description,
            List<String> variables, String defaultContent) {
    }
}
