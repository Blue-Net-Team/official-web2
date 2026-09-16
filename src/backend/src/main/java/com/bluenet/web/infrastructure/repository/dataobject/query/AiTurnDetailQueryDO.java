package com.bluenet.web.infrastructure.repository.dataobject.query;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提问详情投影查询数据对象。
 *
 * <p>
 * 属于 SQL 层投影：intent / action / toolRounds 由 events 提取；events 与 prompt 以
 * PostgreSQL JSONB 的原文返回（JDBC 读取时由 rs.getString 取得 JSON 文本）， 读取层不做派生、聚合或改写。
 * </p>
 */
@Data
public class AiTurnDetailQueryDO {
    /**
     * 会话内序号。
     */
    private Integer seq;
    /**
     * 用户原始提问文本。
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
    /**
     * 原始执行事件序列（JSONB 原文）。
     */
    private String events;
    /**
     * 完整 prompt 快照（JSONB 原文），未执行状态图时为 null。
     */
    private String prompt;
}
