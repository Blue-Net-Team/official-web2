package com.bluenet.web.infrastructure.judge;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmJudgeQueueConfig {
    public static final String ALGORITHM_JUDGE_QUEUE = "bluenet.algorithm.judge";

    @Bean
    public Queue algorithmJudgeQueue() {
        // 主应用当前只负责把判题任务持久化入队，后续由独立消费者处理。
        return new Queue(ALGORITHM_JUDGE_QUEUE, true);
    }
}
