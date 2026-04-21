package com.bluenet.web.domain.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Comment {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 考核作答记录标识。
     */
    private Long answerId;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 答案、题目或评审记录在考核中的得分。
     */
    private BigDecimal score;
    /**
     * 评价或留言提交时间。
     */
    private LocalDateTime commentTime;
}
