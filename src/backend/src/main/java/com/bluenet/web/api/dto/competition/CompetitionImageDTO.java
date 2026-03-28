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
@Schema(description = "竞赛图片信息")
public class CompetitionImageDTO {
    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "图片URL")
    private String url;

    @Schema(description = "图片描述")
    private String description;
}
