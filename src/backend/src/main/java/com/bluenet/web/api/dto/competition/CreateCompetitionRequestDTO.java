package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建竞赛请求")
public class CreateCompetitionRequestDTO {
    @NotBlank(message = "竞赛名称不能为空")
    @Size(max = 100, message = "竞赛名称最多100个字符")
    @Schema(description = "竞赛名称", required = true)
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
}
