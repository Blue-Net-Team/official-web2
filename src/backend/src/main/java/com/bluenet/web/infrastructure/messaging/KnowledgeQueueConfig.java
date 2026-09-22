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
    /** 知识库分片重新嵌入队列。 */
    public static final String KNOWLEDGE_REEMBED_QUEUE = "knowledge.reembed";
    /** 知识库分片重新嵌入路由键。 */
    public static final String KNOWLEDGE_REEMBED_ROUTING_KEY = "re-embed";
    /** 知识库标签向量 upsert 队列。 */
    public static final String KNOWLEDGE_TAG_UPSERT_QUEUE = "knowledge.tag_upsert";
    /** 知识库标签向量 upsert 路由键。 */
    public static final String KNOWLEDGE_TAG_UPSERT_ROUTING_KEY = "tag-upsert";

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

    @Bean
    public Queue knowledgeReembedQueue() {
        return new Queue(KNOWLEDGE_REEMBED_QUEUE, true);
    }

    @Bean
    public Binding knowledgeReembedBinding(Queue knowledgeReembedQueue, DirectExchange knowledgeExchange) {
        return BindingBuilder.bind(knowledgeReembedQueue)
                .to(knowledgeExchange)
                .with(KNOWLEDGE_REEMBED_ROUTING_KEY);
    }

    @Bean
    public Queue knowledgeTagUpsertQueue() {
        return new Queue(KNOWLEDGE_TAG_UPSERT_QUEUE, true);
    }

    @Bean
    public Binding knowledgeTagUpsertBinding(Queue knowledgeTagUpsertQueue, DirectExchange knowledgeExchange) {
        return BindingBuilder.bind(knowledgeTagUpsertQueue)
                .to(knowledgeExchange)
                .with(KNOWLEDGE_TAG_UPSERT_ROUTING_KEY);
    }
}
