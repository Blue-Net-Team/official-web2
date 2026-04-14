package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "竞赛响应信息")
public class CompetitionResponseDTO {
    @Schema(description = "竞赛ID")
    private Long id;

    @Schema(description = "竞赛名称")
    private String name;

    @Schema(description = "竞赛简称")
    private String shortName;

    @Schema(description = "竞赛级别，如：国家级、省级、校级等")
    private String level;

    @Schema(description = "举办月份，如：1月、2月等")
    private String month;

    @Schema(description = "主办单位")
    private String organizer;

    @Schema(description = "竞赛简介")
    private String summary;

    @Schema(description = "Logo 文件ID")
    private Long logoFileId;

    @Schema(description = "封面文件ID")
    private Long coverFileId;
}
