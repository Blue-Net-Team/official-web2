package com.bluenet.web.api.dto.assessment_judgement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 考核评论请求 DTO
 */
@Data
public class CommentRequestDTO {
    @NotNull
    private Long answerId;
    private String content;
    private BigDecimal score;
}
