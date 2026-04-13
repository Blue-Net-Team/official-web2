package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新竞赛请求")
public class UpdateCompetitionRequestDTO {
    @Size(max = 100, message = "竞赛名称最多100个字符")
    @Schema(description = "竞赛名称")
    private String name;

    @Size(max = 50, message = "竞赛简称最多50个字符")
    @Schema(description = "竞赛简称")
    private String shortName;

    @Schema(description = "Logo文件ID")
    private Long logoFileId;

    @Size(max = 500, message = "竞赛简介最多500个字符")
    @Schema(description = "竞赛简介")
    private String summary;

    @Schema(description = "竞赛详细介绍")
    private String detail;

    @Size(max = 20, message = "竞赛级别最多20个字符")
    @Schema(description = "竞赛级别，如：国家级、省级、校级等")
    private String level;

    @Size(max = 10, message = "举办月份最多10个字符")
    @Schema(description = "举办月份，如：1月、2月等")
    private String month;

    @Size(max = 200, message = "主办单位最多200个字符")
    @Schema(description = "主办单位")
    private String organizer;
}
