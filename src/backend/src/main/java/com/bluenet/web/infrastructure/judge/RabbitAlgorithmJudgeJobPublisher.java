package com.bluenet.web.infrastructure.judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitAlgorithmJudgeJobPublisher implements AlgorithmJudgeJobPublisher {
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    @Override
    public void publish(Long judgeJobId, AlgorithmTestcaseType testcaseType) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            // 测试或未启用 RabbitMQ 的环境只保留任务入库，不阻塞当前发布端启动。
            log.warn("RabbitMQ 未启用，算法判题任务仅已入库，judgeJobId={}, testcaseType={}", judgeJobId, testcaseType);
            return;
        }

        rabbitTemplate.convertAndSend(
                AlgorithmJudgeQueueConfig.ALGORITHM_JUDGE_QUEUE,
                // 队列消息只传任务 ID，便于后续独立消费者按相同协议接入。
                judgeJobId.toString());
    }
}
