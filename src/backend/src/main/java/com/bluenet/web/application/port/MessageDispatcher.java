package com.bluenet.web.application.port;

import com.bluenet.web.application.message.MessageRequest;

/**
 * 应用层消息分发端口。
 *
 * <p>
 * port 包只放“应用层对外部能力的抽象接口”；消息请求模型放在 application.message 包。
 * 调用方通过该端口提交消息请求，基础设施层根据通道选择具体发送策略。
 * </p>
 */
public interface MessageDispatcher {

    /**
     * 同步分发消息。
     *
     * @param request
     *            应用层消息请求，由基础设施按通道选择具体策略。
     */
    void dispatch(MessageRequest request);

    /**
     * 异步分发消息。
     *
     * @param request
     *            应用层消息请求，调用方不关心具体通道实现。
     */
    void dispatchAsync(MessageRequest request);
}
