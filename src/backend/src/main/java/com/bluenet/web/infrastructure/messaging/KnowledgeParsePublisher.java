package com.bluenet.web.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 知识库文档解析任务发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeParsePublisher {

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final ObjectMapper objectMapper;

    /**
     * 发布文档解析任务到 RabbitMQ。
     *
     * @param docId
     *            文档ID
     * @param fileId
     *            文件ID
     * @param downloadUrl
     *            预签名下载URL
     * @param reparse
     *            是否为重新解析
     */
    public void publish(Long docId, Long fileId, String downloadUrl, boolean reparse) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            log.warn("RabbitMQ 未启用，知识库解析任务未发布，docId={}", docId);
            return;
        }

        try {
            Map<String, Object> message = Map.of(
                    "docId",
                    docId,
                    "fileId",
                    fileId,
                    "downloadUrl",
                    downloadUrl,
                    "reparse",
                    reparse);
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(
                    KnowledgeQueueConfig.KNOWLEDGE_EXCHANGE,
                    KnowledgeQueueConfig.KNOWLEDGE_PARSE_ROUTING_KEY,
                    jsonMessage);
            log.info("知识库解析任务已发布，docId={}, fileId={}, reparse={}", docId, fileId, reparse);
        } catch (Exception e) {
            log.error("发布知识库解析任务失败，docId={}", docId, e);
            throw new RuntimeException("发布解析任务失败", e);
        }
    }

    /**
     * 发布分片重新嵌入任务到 RabbitMQ。
     *
     * @param chunkId
     *            分段ID
     */
    public void publishReembed(Long chunkId) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            log.warn("RabbitMQ 未启用，re-embed 任务未发布，chunkId={}", chunkId);
            return;
        }

        try {
            Map<String, Object> message = Map.of("chunkId", chunkId);
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(
                    KnowledgeQueueConfig.KNOWLEDGE_EXCHANGE,
                    KnowledgeQueueConfig.KNOWLEDGE_REEMBED_ROUTING_KEY,
                    jsonMessage);
            log.info("分片重新嵌入任务已发布，chunkId={}", chunkId);
        } catch (Exception e) {
            log.error("发布 re-embed 任务失败，chunkId={}", chunkId, e);
            throw new RuntimeException("发布 re-embed 任务失败", e);
        }
    }

    /**
     * 发布标签向量 upsert 任务到 RabbitMQ。
     *
     * @param tagId
     *            标签ID
     * @param recount
     *            是否需要在处理后重新统计标签引用计数（标签删除场景）
     */
    public void publishTagUpsert(Long tagId, boolean recount) {
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            log.warn("RabbitMQ 未启用，tag-upsert 任务未发布，tagId={}", tagId);
            return;
        }

        try {
            Map<String, Object> message = Map.of(
                    "tagId",
                    tagId,
                    "recount",
                    recount);
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(
                    KnowledgeQueueConfig.KNOWLEDGE_EXCHANGE,
                    KnowledgeQueueConfig.KNOWLEDGE_TAG_UPSERT_ROUTING_KEY,
                    jsonMessage);
            log.info("标签向量 upsert 任务已发布，tagId={}", tagId);
        } catch (Exception e) {
            log.error("发布 tag-upsert 任务失败，tagId={}", tagId, e);
            throw new RuntimeException("发布 tag-upsert 任务失败", e);
        }
    }
}
