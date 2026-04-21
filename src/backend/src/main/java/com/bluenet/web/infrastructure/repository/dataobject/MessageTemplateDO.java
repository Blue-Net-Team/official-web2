package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_message_template")
public class MessageTemplateDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
