package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.application.MemberResult;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.util.GradeCalculator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成员应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class MemberAppConverter {

    public MemberBriefDTO toBriefDTO(MemberResult result) {
        return MemberBriefDTO.builder()
                .id(result.id())
                .username(result.username())
                .nickname(result.nickname())
                .direction(result.direction())
                .job(result.job())
                .avatarFileId(result.avatarFileId())
                .college(result.college())
                .major(result.major())
                .enrollmentYear(result.enrollmentYear())
                .gender(result.gender())
                .roleName(result.roleName())
                .build();
    }

    public MemberDetailDTO toDetailDTO(MemberResult result) {
        String gradeLabel = GradeCalculator.getGradeLabel(result.studentId(), result.assessmentGradeYear());

        return MemberDetailDTO.builder()
                .id(result.id())
                .enrollmentYear(result.enrollmentYear())
                .grade(gradeLabel)
                .studentId(result.studentId())
                .username(result.username())
                .nickname(result.nickname())
                .direction(result.direction())
                .job(result.job())
                .avatarFileId(result.avatarFileId())
                .college(result.college())
                .major(result.major())
                .gender(result.gender())
                .role(result.role())
                .bio(result.bio())
                .githubUsername(result.githubUsername())
                .wechatQrcode(result.wechatQrcode())
                .build();
    }

    public List<DirectionLeaderDTO> toDirectionLeaderDTOs(List<MemberResult> leaders) {
        Map<Direction, MemberResult> leaderMap = leaders.stream()
                .collect(Collectors.toMap(MemberResult::direction, result -> result, (a, b) -> a));

        return List.of(Direction.values())
                .stream()
                .map(
                        direction -> DirectionLeaderDTO.builder()
                                .direction(direction)
                                .directionName(direction.getDescription())
                                .leader(toLeaderInfo(leaderMap.get(direction)))
                                .build())
                .collect(Collectors.toList());
    }

    private DirectionLeaderDTO.LeaderInfo toLeaderInfo(MemberResult result) {
        if (result == null) {
            return null;
        }
        return DirectionLeaderDTO.LeaderInfo.builder()
                .id(result.id())
                .username(result.username())
                .nickname(result.nickname())
                .avatarFileId(result.avatarFileId())
                .build();
    }
}
