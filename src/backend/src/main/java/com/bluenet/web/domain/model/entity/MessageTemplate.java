package com.bluenet.web.domain.model.entity;

import lombok.Data;

@Data
public class MessageTemplate {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 验证码、模板编码或业务唯一编码。
     */
    private String code;
    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 消息模板或邮件主题。
     */
    private String subject;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 业务对象的详细描述。
     */
    private String description;
    /**
     * 模板、配置或功能开关是否启用。
     */
    private Boolean enabled;
}
