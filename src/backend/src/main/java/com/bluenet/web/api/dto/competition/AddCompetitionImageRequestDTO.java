package com.bluenet.web.api.dto.competition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "添加竞赛图片请求")
public class AddCompetitionImageRequestDTO {
    @NotNull(message = "文件ID不能为空")
    @Schema(description = "文件ID", required = true)
    private Long fileId;

    @Schema(description = "图片描述")
    private String description;
}
