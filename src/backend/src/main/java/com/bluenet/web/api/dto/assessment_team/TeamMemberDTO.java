package com.bluenet.web.api.dto.assessment_team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 队伍成员数据传输对象
 * <p>
 * 用于API响应中返回队伍成员信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "队伍成员信息")
public class TeamMemberDTO {
    @Schema(description = "成员记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "头像文件ID")
    private Long avatarFileId;

    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;

    @Schema(description = "是否为队长")
    private boolean leader;
}
