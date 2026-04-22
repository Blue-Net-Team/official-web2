package com.bluenet.web.application.message;

/**
 * 消息内容格式。
 *
 * <p>
 * 该类型只描述内容如何被策略解释，不代表投递通道；例如邮箱、站内信都可以发送文本或 HTML。
 * </p>
 */
public enum MessageContentType {
    /**
     * 纯文本内容。
     */
    TEXT,

    /**
     * HTML 内容。
     */
    HTML,

    /**
     * 模板内容，发送策略负责变量替换。
     */
    TEMPLATE
}
