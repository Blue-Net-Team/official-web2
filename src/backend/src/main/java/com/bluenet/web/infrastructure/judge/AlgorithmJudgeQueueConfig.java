package com.bluenet.web.infrastructure.judge;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmJudgeQueueConfig {
    public static final String ALGORITHM_JUDGE_QUEUE = "bluenet.algorithm.judge";

    @Bean
    public Queue algorithmJudgeQueue() {
        // 判题任务需要持久化队列承接，Worker 独立消费这条队列。
        return new Queue(ALGORITHM_JUDGE_QUEUE, true);
    }
}
