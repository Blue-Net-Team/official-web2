package com.bluenet.judge.infrastructure.messaging;

import com.bluenet.judge.application.service.workflow.TestDataGenerationWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 测试数据生成任务消息监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataGenerationListener {
    private final TestDataGenerationWorkflow testDataGenerationWorkflow;

    /**
     * 处理测试数据生成任务消息。
     *
     * @param payload
     *            RabbitMQ 消息体，内容必须是判题配置 ID。
     */
    @RabbitListener(queues = "${judge.rabbitmq.test-data-queue}")
    public void onMessage(String payload) {
        Long configId = parseId(payload);
        log.info("收到测试数据生成任务，配置编号={}", configId);
        testDataGenerationWorkflow.handle(configId);
    }

    /**
     * 将消息体解析为数值主键。
     *
     * @param payload
     *            RabbitMQ 消息体。
     * @return 判题配置主键。
     */
    private Long parseId(String payload) {
        try {
            return Long.valueOf(payload);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("测试数据生成消息体必须是数字配置编号：" + payload, ex);
        }
    }
}
