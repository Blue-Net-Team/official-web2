package com.bluenet.web.infrastructure.judge;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmJudgeQueueConfig {
    /** 判题服务直连交换机名称。所有判题相关消息（正式判题、自测、测试数据生成）均通过此交换机路由。 */
    public static final String ALGORITHM_JUDGE_EXCHANGE = "algorithm.judge";
    /** 正式判题队列。接收考生提交的算法题判题任务，由 judge-service 消费处理。 */
    public static final String ALGORITHM_JUDGE_QUEUE = "algorithm.judge.formal";
    /** 自测/运行测试判题队列。接收考生调试时的即时运行任务，同样由 judge-service 消费。 */
    public static final String ALGORITHM_RUN_JUDGE_QUEUE = "algorithm.judge.run";
    /** 测试数据生成队列。接收管理员触发的测试数据生成任务，由 judge-service 消费执行。 */
    public static final String ALGORITHM_TEST_DATA_QUEUE = "algorithm.judge.test-data";
    /** 正式判题路由键。用于将正式判题消息绑定到 {@link #ALGORITHM_JUDGE_QUEUE}。 */
    public static final String FORMAL_JUDGE_ROUTING_KEY = "judge.formal";
    /** 自测判题路由键。用于将运行测试消息绑定到 {@link #ALGORITHM_RUN_JUDGE_QUEUE}。 */
    public static final String RUN_JUDGE_ROUTING_KEY = "judge.run";
    /** 测试数据生成路由键。用于将测试数据生成消息绑定到 {@link #ALGORITHM_TEST_DATA_QUEUE}。 */
    public static final String TEST_DATA_ROUTING_KEY = "judge.test-data";

    @Bean
    public DirectExchange algorithmJudgeExchange() {
        // Judge tasks share one durable exchange; payloads stay small and contain only
        // ids.
        return new DirectExchange(ALGORITHM_JUDGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue algorithmJudgeQueue() {
        // 主应用当前只负责把判题任务持久化入队，后续由独立消费者处理。
        return new Queue(ALGORITHM_JUDGE_QUEUE, true);
    }

    @Bean
    public Queue algorithmRunJudgeQueue() {
        // 运行测试（自测）判题任务队列，由独立消费者处理。
        return new Queue(ALGORITHM_RUN_JUDGE_QUEUE, true);
    }

    @Bean
    public Queue algorithmTestDataQueue() {
        // 测试数据生成只传配置 ID，由 Judge Service 回查 DB 和 OSS。
        return new Queue(ALGORITHM_TEST_DATA_QUEUE, true);
    }

    @Bean
    public Binding algorithmJudgeBinding(Queue algorithmJudgeQueue, DirectExchange algorithmJudgeExchange) {
        return BindingBuilder.bind(algorithmJudgeQueue)
                .to(algorithmJudgeExchange)
                .with(FORMAL_JUDGE_ROUTING_KEY);
    }

    @Bean
    public Binding algorithmRunJudgeBinding(Queue algorithmRunJudgeQueue, DirectExchange algorithmJudgeExchange) {
        return BindingBuilder.bind(algorithmRunJudgeQueue)
                .to(algorithmJudgeExchange)
                .with(RUN_JUDGE_ROUTING_KEY);
    }

    @Bean
    public Binding algorithmTestDataBinding(Queue algorithmTestDataQueue, DirectExchange algorithmJudgeExchange) {
        return BindingBuilder.bind(algorithmTestDataQueue)
                .to(algorithmJudgeExchange)
                .with(TEST_DATA_ROUTING_KEY);
    }
}
