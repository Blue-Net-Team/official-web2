package com.bluenet.web.api.dto.assessment_team;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍预览响应DTO
 * <p>
 * 用于通过邀请码预览队伍信息的响应
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "队伍预览信息")
public class TeamPreviewResponseDTO {
    @Schema(description = "队伍ID")
    private Long id;

    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;

    @Schema(description = "队长用户名")
    private String leaderUsername;

    @Schema(description = "队伍名称")
    private String name;

    @Schema(description = "队伍状态")
    private AssessmentTeam.TeamStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "当前成员数量")
    private int memberCount;

    @Schema(description = "成员用户名列表")
    private List<String> memberUsernames;
}
