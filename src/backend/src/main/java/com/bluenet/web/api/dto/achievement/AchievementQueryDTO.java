package com.bluenet.web.api.dto.achievement;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "成就查询参数")
public class AchievementQueryDTO {
    @Schema(description = "成就类型，默认返回全部", example = "COMPETITION")
    private AchievementType type;

    @Schema(description = "奖项级别，仅对竞赛成就有效", example = "NATIONAL")
    private AwardLevel awardLevel;

    @Schema(description = "获奖年份")
    private Integer year;
}
