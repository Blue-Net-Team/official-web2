package com.bluenet.web.infrastructure.judge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitJudgeTestDataGenerationPublisher implements JudgeTestDataGenerationPublisher {
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    /**
     * 发布测试数据生成任务。
     *
     * @param configId
     *            判题配置主键，消息体只包含该 ID。
     */
    @Override
    public void publish(Long configId) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            // 测试环境允许只保存配置，不因为 RabbitMQ 缺失阻塞管理端流程。
            log.warn("RabbitMQ 未启用，测试数据生成任务未发送，configId={}", configId);
            return;
        }
        rabbitTemplate.convertAndSend(
                AlgorithmJudgeQueueConfig.ALGORITHM_JUDGE_EXCHANGE,
                AlgorithmJudgeQueueConfig.TEST_DATA_ROUTING_KEY,
                configId.toString());
    }
}
