package com.bluenet.web.api.dto.assessment_judgement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认最终评分请求 DTO
 */
@Data
public class FinalizeScoreRequestDTO {
    @NotNull(message = "答案ID不能为空")
    private Long answerId;
    @NotNull(message = "最终得分不能为空")
    private BigDecimal score;
}
