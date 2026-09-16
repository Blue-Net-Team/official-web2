package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话摘要，用于会话列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversationSummary {
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
     * 消息数：该会话下的用户提问数量，不计助手回复。
     */
    private Long messageCount;
    /**
     * 内嵌的提问摘要列表，最多内联前若干条。
     */
    private List<AiTurnSummary> turns;
    /**
     * 未被内联的提问数量，为 0 表示已全部内联。
     */
    private Long hiddenTurnCount;
}
