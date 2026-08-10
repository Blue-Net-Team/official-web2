package com.bluenet.web.api.dto.githuborg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "单个用户 GitHub 组织邀请结果")
public class GitHubOrgInviteDetailResponseDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否成功（已受邀或已是成员也视为成功）")
    private boolean success;

    @Schema(description = "结果说明（成功或失败原因）")
    private String reason;
}
