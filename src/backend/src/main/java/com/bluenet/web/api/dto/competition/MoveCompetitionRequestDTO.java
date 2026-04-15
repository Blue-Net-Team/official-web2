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
@Schema(description = "移动竞赛排序请求")
public class MoveCompetitionRequestDTO {
    @NotNull(message = "移动方向不能为空")
    @Schema(description = "移动方向：UP（上移）或 DOWN（下移）", required = true, allowableValues = { "UP", "DOWN" })
    private String direction;
}
