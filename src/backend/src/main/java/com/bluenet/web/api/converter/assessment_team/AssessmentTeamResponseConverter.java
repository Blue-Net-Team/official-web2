package com.bluenet.web.api.converter.assessment_team;

import com.bluenet.web.api.dto.assessment_team.AssessmentTeamDTO;
import com.bluenet.web.api.dto.assessment_team.TeamMemberDTO;
import com.bluenet.web.application.TeamResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 考核队伍响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentTeamResponseConverter {

    /**
     * 将应用层队伍结果转换为 DTO
     */
    public AssessmentTeamDTO toDTO(TeamResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentTeamDTO.builder()
                .id(result.id())
                .assessmentTimeId(result.assessmentTimeId())
                .leaderId(result.leaderId())
                .name(result.name())
                .inviteCode(result.inviteCode())
                .status(result.status())
                .createdAt(result.createdAt())
                .members(toMemberDTOs(result.members()))
                .build();
    }

    private List<TeamMemberDTO> toMemberDTOs(List<TeamResult.TeamMemberResult> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(this::toMemberDTO)
                .toList();
    }

    private TeamMemberDTO toMemberDTO(TeamResult.TeamMemberResult member) {
        return TeamMemberDTO.builder()
                .id(member.id())
                .userId(member.userId())
                .username(member.username())
                .direction(member.direction())
                .avatarFileId(member.avatarFileId())
                .joinedAt(member.joinedAt())
                .leader(member.isLeader())
                .build();
    }
}
