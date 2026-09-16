package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 对话概览指标")
public class AiTraceOverviewDTO {
    @Schema(description = "会话总数")
    private Long conversationCount;
    @Schema(description = "提问总数")
    private Long questionCount;
    @Schema(description = "被拒答的提问数")
    private Long refusedCount;
    @Schema(description = "触发兜底语义检索的提问数")
    private Long fallbackCount;
    @Schema(description = "兜底检索率")
    private Double fallbackRate;
}
