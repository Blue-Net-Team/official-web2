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
@Schema(description = "成就查询参数")
public class AchievementQueryDTO {
    @Schema(description = "成就类型：paper/patent/competition，默认返回全部")
    private String type;

    @Schema(description = "奖项级别：national/provincial/school，仅对竞赛成就有效")
    private String awardLevel;

    @Schema(description = "获奖年份")
    private Integer year;
}
