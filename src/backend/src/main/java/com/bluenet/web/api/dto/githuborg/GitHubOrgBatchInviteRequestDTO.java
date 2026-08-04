package com.bluenet.web.api.dto.githuborg;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "批量邀请用户加入 GitHub 组织请求")
public class GitHubOrgBatchInviteRequestDTO {

    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 50, message = "一次最多邀请50个用户")
    @Schema(description = "用户ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> userIds;
}
