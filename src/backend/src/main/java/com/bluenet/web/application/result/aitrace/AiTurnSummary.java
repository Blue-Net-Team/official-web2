package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提问摘要，用于会话列表内嵌展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTurnSummary {
    /**
     * 会话内序号，从 1 开始。
     */
    private Integer seq;
    /**
     * 用户原始提问文本。
     */
    private String userInput;
    /**
     * 意图标签，取自意图判定事件。
     */
    private String intent;
    /**
     * 处理动作：RETRIEVE / REFUSE / DIRECT。
     */
    private String action;
    /**
     * 该提问内的工具调用轮次。
     */
    private Integer toolRounds;
    /**
     * 该提问的耗时（毫秒）。
     */
    private Integer durationMs;
    /**
     * 是否为不完整记录。
     */
    private Boolean degraded;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
}
