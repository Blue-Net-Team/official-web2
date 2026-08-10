package com.bluenet.web.api.dto.achievement;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新成就请求")
public class UpdateAchievementRequestDTO {
    @NotBlank(message = "成就标题不能为空")
    @Size(max = 200, message = "成就标题最多200个字符")
    @Schema(description = "成就标题", required = true, example = "蓝桥杯全国一等奖")
    private String title;

    @NotNull(message = "成就类型不能为空")
    @Schema(description = "成就类型", required = true, example = "COMPETITION")
    private AchievementType type;

    @Size(max = 100, message = "关联项最多100个字符")
    @Schema(description = "关联项：竞赛为赛项名，论文为期刊名，专利为null", example = "蓝桥杯")
    private String relateTo;

    @NotNull(message = "获奖日期不能为空")
    @PastOrPresent(message = "获奖日期不能是未来日期")
    @Schema(description = "获奖日期", required = true, example = "2024-04-15")
    private LocalDate achieveAt;

    @Schema(description = "奖项级别，竞赛成就必填", example = "NATIONAL")
    private AwardLevel awardLevel;

    @Size(max = 50, message = "奖项名称最多50个字符")
    @Schema(description = "奖项名称：一等奖/二等奖/三等奖", example = "一等奖")
    private String awardName;

    @NotNull(message = "成就图片不能为空")
    @Schema(description = "成就图片文件ID", required = true, example = "123")
    private Long fileId;

    @Schema(description = "关联的系统内成员用户ID列表", example = "[1, 2]")
    private List<Long> userIds;

    @Size(max = 20, message = "外部协作者最多20个")
    @Schema(description = "外部协作者姓名列表（非系统用户）", example = "[\"张三-外校\", \"李四-他队\"]")
    private List<@Size(max = 100, message = "外部协作者姓名不能超过100字符") String> externalMembers;
}
