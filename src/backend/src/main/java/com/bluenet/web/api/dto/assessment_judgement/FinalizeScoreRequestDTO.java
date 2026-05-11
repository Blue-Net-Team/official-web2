package com.bluenet.web.api.dto.assessment_judgement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认最终评分请求 DTO
 */
@Data
public class FinalizeScoreRequestDTO {
    @NotNull
    private Long answerId;
    @NotNull
    private BigDecimal score;
    private String comment;
}
