package com.bluenet.web.api.dto.assessment_team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 离开队伍请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "离开队伍请求")
public class LeaveTeamRequestDTO {
    @NotNull(message = "队伍ID不能为空")
    @Schema(description = "队伍ID", required = true)
    private Long teamId;
}
