package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话概览指标。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiTraceOverview {
    /**
     * 会话总数。
     */
    private Long conversationCount;
    /**
     * 提问总数。
     */
    private Long questionCount;
    /**
     * 被拒答的提问数。
     */
    private Long refusedCount;
    /**
     * 触发兜底语义检索的提问数。
     */
    private Long fallbackCount;
    /**
     * 兜底检索率：触发兜底的提问数 / 提问总数；无提问时为 0。
     */
    private Double fallbackRate;
}
