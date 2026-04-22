package com.bluenet.web.application.message;

/**
 * 消息投递通道。
 *
 * <p>
 * 应用层用该枚举声明“消息要发到哪里”，基础设施层按通道选择具体发送策略。 当前只有邮箱通道，后续可扩展短信、站内信、Webhook 等通道。
 * </p>
 */
public enum MessageChannel {
    /**
     * 邮箱通道。
     */
    EMAIL
}
