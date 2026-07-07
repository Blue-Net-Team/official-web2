package com.bluenet.web.application;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评论应用层结果对象。
 */
@Builder
public record CommentResult(
        Long id,
        Long answerId,
        Long userId,
        String username,
        String content,
        BigDecimal score,
        LocalDateTime commentTime) {
}
