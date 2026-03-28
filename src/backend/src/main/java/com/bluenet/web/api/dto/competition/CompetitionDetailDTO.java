package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "竞赛详细信息")
public class CompetitionDetailDTO {
    @Schema(description = "竞赛ID")
    private Long id;

    @Schema(description = "竞赛名称")
    private String name;

    @Schema(description = "竞赛简称")
    private String shortName;

    @Deprecated
    @Schema(description = "Logo URL (已废弃，请使用 logoFileId)")
    private String logoUrl;

    @Schema(description = "Logo 文件ID")
    private Long logoFileId;

    @Schema(description = "竞赛简介")
    private String summary;

    @Schema(description = "竞赛详细介绍")
    private String detail;

    @Schema(description = "竞赛相关照片")
    private List<CompetitionImageDTO> images;
}
