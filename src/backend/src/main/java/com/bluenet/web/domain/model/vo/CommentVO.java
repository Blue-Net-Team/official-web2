package com.bluenet.web.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考核评论领域值对象
 */
@Data
@Builder
public class CommentVO {
    private Long id;
    private Long answerId;
    private Long userId;
    private String username;
    private String content;
    private BigDecimal score;
    private LocalDateTime commentTime;
}
