package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提问摘要投影查询数据对象。
 *
 * <p>
 * 属于 SQL 层投影：intent / action 从 events JSONB 中提取，toolRounds 取工具调用的最大轮次。
 * </p>
 */
@Data
public class AiTurnSummaryQueryDO {
    /**
     * 所属会话标识。
     */
    private String conversationId;
    /**
     * 会话内序号。
     */
    private Integer seq;
    /**
     * 用户原始提问文本。
     */
    private String userInput;
    /**
     * 意图标签。
     */
    private String intent;
    /**
     * 处理动作。
     */
    private String action;
    /**
     * 工具调用轮次。
     */
    private Integer toolRounds;
    /**
     * 耗时（毫秒）。
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
