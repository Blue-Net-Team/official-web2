package com.bluenet.web.api.dto.assessment_team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加入队伍请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "加入队伍请求")
public class JoinTeamRequestDTO {
    @NotBlank(message = "邀请码不能为空")
    @Schema(description = "邀请码", required = true)
    private String inviteCode;
}
