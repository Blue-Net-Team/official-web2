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
 * 考核队伍数据传输对象
 * <p>
 * 用于API响应中返回队伍信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核队伍信息")
public class AssessmentTeamDTO {
    @Schema(description = "队伍ID")
    private Long id;

    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;

    @Schema(description = "队长ID")
    private Long leaderId;

    @Schema(description = "队伍名称")
    private String name;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "队伍状态")
    private AssessmentTeam.TeamStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "成员列表")
    private List<TeamMemberDTO> members;
}
