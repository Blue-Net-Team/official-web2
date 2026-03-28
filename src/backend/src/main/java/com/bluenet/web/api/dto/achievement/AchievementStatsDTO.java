package com.bluenet.web.api.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "成就统计信息")
public class AchievementStatsDTO {
    @Schema(description = "总成就数")
    private Long totalAchievements;

    @Schema(description = "国家级奖项数")
    private Long nationalCount;

    @Schema(description = "省级奖项数")
    private Long provincialCount;

    @Schema(description = "校级奖项数")
    private Long schoolCount;
}
