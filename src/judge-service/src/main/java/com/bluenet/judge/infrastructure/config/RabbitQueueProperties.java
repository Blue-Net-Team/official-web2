package com.bluenet.judge.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 判题 RabbitMQ 队列配置。
 *
 * @param formalJudgeQueue
 *            正式判题任务队列。
 * @param testDataQueue
 *            测试数据生成任务队列。
 * @param exchange
 *            判题任务交换机。
 * @param formalJudgeRoutingKey
 *            正式判题 routing key。
 * @param testDataRoutingKey
 *            测试数据生成 routing key。
 */
@Validated
@ConfigurationProperties(prefix = "judge.rabbitmq")
public record RabbitQueueProperties(
        @NotBlank String formalJudgeQueue,
        @NotBlank String testDataQueue,
        @NotBlank String runJudgeQueue,
        @NotBlank String exchange,
        @NotBlank String formalJudgeRoutingKey,
        @NotBlank String testDataRoutingKey,
        @NotBlank String runJudgeRoutingKey) {
}
