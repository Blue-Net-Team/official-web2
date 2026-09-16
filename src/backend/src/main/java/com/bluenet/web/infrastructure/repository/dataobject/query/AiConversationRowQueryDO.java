package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表聚合查询数据对象，仅用于承接 XML 查询结果。
 *
 * <p>
 * 属于 SQL 层投影：messageCount 由 tb_ai_turn 聚合得出，不落库冗余字段。
 * </p>
 */
@Data
public class AiConversationRowQueryDO {
    /**
     * 会话标识。
     */
    private String id;
    /**
     * 会话创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 会话最后活跃时间。
     */
    private LocalDateTime lastActiveAt;
    /**
     * 用户提问数量（消息数）。
     */
    private Long messageCount;
}
