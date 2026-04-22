package com.bluenet.web.infrastructure.message;

import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageRequest;

/**
 * 基础设施消息发送策略。
 */
public interface MessageSenderStrategy {

    /**
     * 当前策略支持的消息通道。
     */
    MessageChannel channel();

    /**
     * 使用当前策略发送消息。
     *
     * @param request
     *            已通过通道匹配的消息请求。
     */
    void send(MessageRequest request);
}
