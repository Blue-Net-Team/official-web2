package com.bluenet.web.api.dto.assessment_team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 转让队长请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "转让队长请求")
public class TransferLeaderRequestDTO {
    @NotNull(message = "队伍ID不能为空")
    @Schema(description = "队伍ID", required = true)
    private Long teamId;

    @NotNull(message = "新队长用户ID不能为空")
    @Schema(description = "新队长用户ID", required = true)
    private Long newLeaderId;
}
