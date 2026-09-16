package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

/**
 * 概览指标投影查询数据对象。
 *
 * <p>
 * 属于 SQL 层投影：四个计数在同一条聚合查询中从 tb_ai_conversation 与 tb_ai_turn 汇总。
 * </p>
 */
@Data
public class AiOverviewQueryDO {
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
}
