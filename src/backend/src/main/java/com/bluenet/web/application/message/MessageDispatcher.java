package com.bluenet.web.application.message;

/**
 * 应用层消息分发端口。
 *
 * <p>
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
