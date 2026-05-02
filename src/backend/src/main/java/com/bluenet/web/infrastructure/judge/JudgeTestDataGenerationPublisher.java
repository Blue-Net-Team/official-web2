package com.bluenet.web.infrastructure.judge;

public interface JudgeTestDataGenerationPublisher {
    /**
     * 发布测试数据生成任务，消息体只包含配置 ID。
     */
    void publish(Long configId);
}
