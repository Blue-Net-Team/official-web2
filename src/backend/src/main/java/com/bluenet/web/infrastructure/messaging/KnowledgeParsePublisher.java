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
}
