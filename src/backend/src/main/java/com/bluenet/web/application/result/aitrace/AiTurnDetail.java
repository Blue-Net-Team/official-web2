package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提问详情，含完整原始事件序列与 prompt 快照。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTurnDetail {
    /**
     * 会话内序号，从 1 开始。
     */
    private Integer seq;
    /**
     * 用户原始提问文本，不含预披露富化内容。
     */
    private String userInput;
    /**
     * 最终答案。
     */
    private String answer;
    /**
     * 意图标签。
     */
    private String intent;
    /**
     * 处理动作。
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
    /**
     * 原始执行事件序列（JSON 文本），读取层不做派生、聚合或改写。
     */
    private String events;
    /**
     * 完整 prompt 快照（JSON 文本），未执行状态图时为 null。
     */
    private String prompt;
}
