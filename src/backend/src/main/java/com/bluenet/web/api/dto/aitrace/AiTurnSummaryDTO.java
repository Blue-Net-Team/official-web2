package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 提问摘要")
public class AiTurnSummaryDTO {
    @Schema(description = "会话内序号，从 1 开始")
    private Integer seq;
    @Schema(description = "用户原始提问文本")
    private String userInput;
    @Schema(description = "意图标签")
    private String intent;
    @Schema(description = "处理动作：RETRIEVE / REFUSE / DIRECT")
    private String action;
    @Schema(description = "该提问内的工具调用轮次")
    private Integer toolRounds;
    @Schema(description = "耗时（毫秒）")
    private Integer durationMs;
    @Schema(description = "是否为不完整记录")
    private Boolean degraded;
    @Schema(description = "记录创建时间")
    private LocalDateTime createdAt;
}
