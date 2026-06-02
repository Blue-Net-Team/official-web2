package com.bluenet.web.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库文档解析 RabbitMQ 队列配置。
 */
@Configuration
public class KnowledgeQueueConfig {

    /** 知识库解析直连交换机名称。 */
    public static final String KNOWLEDGE_EXCHANGE = "knowledge";
    /** 知识库文档解析队列。 */
    public static final String KNOWLEDGE_PARSE_QUEUE = "knowledge.parse";
    /** 知识库文档解析路由键。 */
    public static final String KNOWLEDGE_PARSE_ROUTING_KEY = "parse";

    @Bean
    public DirectExchange knowledgeExchange() {
        return new DirectExchange(KNOWLEDGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue knowledgeParseQueue() {
        return new Queue(KNOWLEDGE_PARSE_QUEUE, true);
    }

    @Bean
    public Binding knowledgeParseBinding(Queue knowledgeParseQueue, DirectExchange knowledgeExchange) {
        return BindingBuilder.bind(knowledgeParseQueue)
                .to(knowledgeExchange)
                .with(KNOWLEDGE_PARSE_ROUTING_KEY);
    }
}
