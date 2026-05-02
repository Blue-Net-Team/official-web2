package com.bluenet.judge.infrastructure.messaging;

import com.bluenet.judge.application.service.workflow.FormalJudgeWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 正式判题任务消息监听器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormalJudgeJobListener {
    private final FormalJudgeWorkflow formalJudgeWorkflow;

    /**
     * 处理正式判题任务消息。
     *
     * @param payload
     *            RabbitMQ 消息体，内容必须是判题任务 ID。
     */
    @RabbitListener(queues = "${judge.rabbitmq.formal-judge-queue}")
    public void onMessage(String payload) {
        Long jobId = parseId(payload);
        log.info("收到正式判题任务，任务编号={}", jobId);
        formalJudgeWorkflow.handle(jobId);
    }

    /**
     * 处理运行判题任务消息。
     *
     * @param payload
     *            RabbitMQ 消息体，内容必须是判题任务 ID。
     */
    @RabbitListener(queues = "${judge.rabbitmq.run-judge-queue}")
    public void onRunMessage(String payload) {
        Long jobId = parseJobId(payload, "运行判题");
        log.info("收到运行判题任务，任务编号={}", jobId);
        formalJudgeWorkflow.handle(jobId);
    }

    /**
     * 将消息体解析为数值主键。
     *
     * @param payload
     *            RabbitMQ 消息体。
     * @return 判题任务主键。
     */
    private Long parseId(String payload) {
        return parseJobId(payload, "正式判题");
    }

    /**
     * 将消息体解析为数值主键。
     *
     * @param payload
     *            RabbitMQ 消息体。
     * @param taskType
     *            任务类型描述。
     * @return 判题任务主键。
     */
    private Long parseJobId(String payload, String taskType) {
        try {
            return Long.valueOf(payload);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(taskType + "消息体必须是数字任务编号：" + payload, ex);
        }
    }
}
