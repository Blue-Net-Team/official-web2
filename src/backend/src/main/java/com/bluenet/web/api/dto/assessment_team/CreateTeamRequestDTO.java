package com.bluenet.web.api.dto.assessment_team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建队伍请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建队伍请求")
public class CreateTeamRequestDTO {
    @NotNull(message = "考核时间ID不能为空")
    @Schema(description = "考核时间ID", required = true)
    private Long assessmentTimeId;

    @NotBlank(message = "队伍名称不能为空")
    @Schema(description = "队伍名称", required = true)
    private String name;
}
