package com.bluenet.web.application.message;

import java.util.List;

/**
 * 消息模板元数据信息。
 *
 * @param code
 *            模板唯一编码。
 * @param name
 *            模板名称。
 * @param subject
 *            邮件主题。
 * @param description
 *            模板描述。
 * @param variables
 *            可用变量列表。
 * @param content
 *            当前模板内容（可能被覆盖）。
 * @param defaultContent
 *            默认模板内容。
 * @param enabled
 *            是否启用。
 */
public record MessageTemplateInfo(
        String code,
        String name,
        String subject,
        String description,
        List<String> variables,
        String content,
        String defaultContent,
        boolean enabled) {
}
