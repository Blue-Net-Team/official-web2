package com.bluenet.web.application.message;

import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.MessageContentType;
import lombok.Builder;

import java.util.Map;

/**
 * 消息分发请求模型。
 *
 * <p>
 * 这是 application 层提交给消息端口的命令参数，不是端口本身；端口接口保留在 application.message 包。
 * </p>
 */
@Builder
public record MessageRequest(
        /**
         * 消息投递通道，例如邮箱、短信或站内信。
         */
        MessageChannel channel,
        /**
         * 接收方标识；邮箱通道下为邮箱地址。
         */
        String recipient,
        /**
         * 消息标题；不支持标题的通道可由策略忽略。
         */
        String subject,
        /**
         * 消息正文或模板内容。
         */
        String content,
        /**
         * 消息内容格式，决定策略如何解释 content。
         */
        MessageContentType contentType,
        /**
         * 模板变量，仅在 TEMPLATE 内容格式下使用。
         */
        Map<String, String> variables) {

    /**
     * 创建纯文本消息请求。
     *
     * @param channel
     *            消息投递通道。
     * @param recipient
     *            接收方标识。
     * @param subject
     *            消息标题。
     * @param content
     *            纯文本正文。
     * @return 纯文本消息请求。
     */
    public static MessageRequest text(MessageChannel channel, String recipient, String subject, String content) {
        return MessageRequest.builder()
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .contentType(MessageContentType.TEXT)
                .build();
    }

    /**
     * 创建 HTML 消息请求。
     *
     * @param channel
     *            消息投递通道。
     * @param recipient
     *            接收方标识。
     * @param subject
     *            消息标题。
     * @param content
     *            HTML 正文。
     * @return HTML 消息请求。
     */
    public static MessageRequest html(MessageChannel channel, String recipient, String subject, String content) {
        return MessageRequest.builder()
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .contentType(MessageContentType.HTML)
                .build();
    }

    /**
     * 创建模板消息请求，变量替换由具体发送策略执行。
     *
     * @param channel
     *            消息投递通道。
     * @param recipient
     *            接收方标识。
     * @param subject
     *            消息标题。
     * @param templateContent
     *            模板正文。
     * @param variables
     *            模板变量。
     * @return 模板消息请求。
     */
    public static MessageRequest template(MessageChannel channel, String recipient, String subject,
            String templateContent, Map<String, String> variables) {
        return MessageRequest.builder()
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .content(templateContent)
                .contentType(MessageContentType.TEMPLATE)
                .variables(variables)
                .build();
    }
}
