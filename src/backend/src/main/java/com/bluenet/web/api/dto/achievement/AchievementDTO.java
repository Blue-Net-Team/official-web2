package com.bluenet.web.api.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "成就信息")
public class AchievementDTO {
    @Schema(description = "成就ID")
    private Long id;

    @Schema(description = "成就标题")
    private String title;

    @Schema(description = "关联项：竞赛为赛项名，论文为期刊名，专利为null")
    private String relateTo;

    @Schema(description = "成就类型：paper/patent/competition")
    private String type;

    @Schema(description = "获奖日期")
    private LocalDate achieveAt;

    @Schema(description = "奖项级别：national/provincial/school")
    private String awardLevel;

    @Schema(description = "奖项级别名称：国家级/省级/校级")
    private String awardLevelName;

    @Schema(description = "奖项名称：一等奖/二等奖/三等奖")
    private String awardName;

    @Schema(description = "竞赛名称")
    private String competitionName;

    @Schema(description = "竞赛简称")
    private String competitionShortName;

    @Schema(description = "竞赛Logo文件ID")
    private Long competitionLogoFileId;
}
