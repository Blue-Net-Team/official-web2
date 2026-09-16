package com.bluenet.web.application.result.aitrace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话详情，含会话元信息与全部提问轨迹。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversationDetail {
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
     * 消息数：该会话下的用户提问数量。
     */
    private Long messageCount;
    /**
     * 按序号排列的全部提问轨迹。
     */
    private List<AiTurnDetail> turns;
}
