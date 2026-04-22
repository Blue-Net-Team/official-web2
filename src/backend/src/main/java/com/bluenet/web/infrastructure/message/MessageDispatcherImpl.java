package com.bluenet.web.infrastructure.message;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于消息通道路由到具体发送策略。
 */
@Component
@RequiredArgsConstructor
public class MessageDispatcherImpl implements MessageDispatcher {
    private final List<MessageSenderStrategy> strategies;

    @Override
    public void dispatch(MessageRequest request) {
        // 策略列表由 Spring 注入；按请求通道选择唯一发送策略。
        MessageSenderStrategy strategy = strategies.stream()
                .filter(candidate -> candidate.channel() == request.channel())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported message channel: " + request.channel()));
        strategy.send(request);
    }

    @Override
    @Async
    public void dispatchAsync(MessageRequest request) {
        // 异步入口只负责线程切换，实际路由逻辑复用同步分发。
        dispatch(request);
    }

}
