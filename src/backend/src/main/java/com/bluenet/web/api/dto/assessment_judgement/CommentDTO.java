package com.bluenet.web.api.dto.assessment_judgement;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考核评论 DTO
 */
@Data
@Builder
public class CommentDTO {
    private Long id;
    private Long answerId;
    private Long userId;
    private String username;
    private String content;
    private BigDecimal score;
    private LocalDateTime commentTime;
}
