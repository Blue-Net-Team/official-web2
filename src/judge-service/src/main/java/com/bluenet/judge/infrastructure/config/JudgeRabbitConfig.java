package com.bluenet.judge.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Judge Service RabbitMQ 队列和绑定配置。
 */
@Configuration
@RequiredArgsConstructor
public class JudgeRabbitConfig {
    private final RabbitQueueProperties properties;

    /**
     * 创建判题任务交换机。
     *
     * @return 判题任务 direct exchange。
     */
    @Bean
    public DirectExchange judgeExchange() {
        // 后端只向该持久化交换机发送 ID 形式的小消息。
        return new DirectExchange(properties.exchange(), true, false);
    }

    /**
     * 创建正式判题任务队列。
     *
     * @return 正式判题任务队列。
     */
    @Bean
    public Queue formalJudgeQueue() {
        return new Queue(properties.formalJudgeQueue(), true);
    }

    /**
     * 创建测试数据生成任务队列。
     *
     * @return 测试数据生成任务队列。
     */
    @Bean
    public Queue testDataQueue() {
        return new Queue(properties.testDataQueue(), true);
    }

    /**
     * 创建运行判题任务队列。
     *
     * @return 运行判题任务队列。
     */
    @Bean
    public Queue runJudgeQueue() {
        return new Queue(properties.runJudgeQueue(), true);
    }

    /**
     * 绑定正式判题队列到判题交换机。
     *
     * @param formalJudgeQueue
     *            正式判题队列。
     * @param judgeExchange
     *            判题任务交换机。
     * @return RabbitMQ 绑定关系。
     */
    @Bean
    public Binding formalJudgeBinding(Queue formalJudgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(formalJudgeQueue).to(judgeExchange).with(properties.formalJudgeRoutingKey());
    }

    /**
     * 绑定测试数据生成队列到判题交换机。
     *
     * @param testDataQueue
     *            测试数据生成队列。
     * @param judgeExchange
     *            判题任务交换机。
     * @return RabbitMQ 绑定关系。
     */
    @Bean
    public Binding testDataBinding(Queue testDataQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(testDataQueue).to(judgeExchange).with(properties.testDataRoutingKey());
    }

    /**
     * 绑定运行判题队列到判题交换机。
     *
     * @param runJudgeQueue
     *            运行判题队列。
     * @param judgeExchange
     *            判题任务交换机。
     * @return RabbitMQ 绑定关系。
     */
    @Bean
    public Binding runJudgeBinding(Queue runJudgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(runJudgeQueue).to(judgeExchange).with(properties.runJudgeRoutingKey());
    }
}
