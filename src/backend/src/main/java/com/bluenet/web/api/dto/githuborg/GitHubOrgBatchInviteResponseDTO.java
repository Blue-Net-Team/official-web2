package com.bluenet.web.api.dto.githuborg;

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
@Schema(description = "批量 GitHub 组织邀请结果")
public class GitHubOrgBatchInviteResponseDTO {

    @Schema(description = "邀请总数")
    private int total;

    @Schema(description = "成功数")
    private int succeeded;

    @Schema(description = "失败数")
    private int failed;

    @Schema(description = "每个用户的邀请结果")
    private List<GitHubOrgInviteDetailResponseDTO> details;
}
