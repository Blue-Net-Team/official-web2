package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI 对话统计聚合")
public class AiTraceStatisticsDTO {
    @Schema(description = "概览指标")
    private AiTraceOverviewDTO overview;
    @Schema(description = "会话量趋势（按会话聚合，无数据的时间桶为 0）")
    private List<AiTrendPointDTO> trend;
    @Schema(description = "意图分布（按提问聚合）")
    private List<AiCountItemDTO> intents;
    @Schema(description = "动作分布（按提问聚合）")
    private List<AiCountItemDTO> actions;
    @Schema(description = "检索工具使用分布")
    private List<AiCountItemDTO> tools;
    @Schema(description = "拒答原因分布")
    private List<AiCountItemDTO> refusals;
}
