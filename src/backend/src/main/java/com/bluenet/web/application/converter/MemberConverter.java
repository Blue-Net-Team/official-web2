package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MemberConverter {

    public MemberBriefDTO toBriefDTO(MemberVO vo) {
        return MemberBriefDTO.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .direction(vo.getDirection())
                .job(vo.getJob())
                .avatarFileId(vo.getAvatarFileId())
                .college(vo.getCollege())
                .major(vo.getMajor())
                .enrollmentYear(vo.getEnrollmentYear())
                .gender(vo.getGender())
                .roleName(vo.getRoleName())
                .build();
    }

    public MemberDetailDTO toDetailDTO(MemberVO vo) {
        return MemberDetailDTO.builder()
                .id(vo.getId())
                .enrollmentYear(vo.getEnrollmentYear())
                .studentId(vo.getStudentId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .direction(vo.getDirection())
                .job(vo.getJob())
                .avatarFileId(vo.getAvatarFileId())
                .college(vo.getCollege())
                .major(vo.getMajor())
                .gender(vo.getGender())
                .role(vo.getRole())
                .bio(vo.getBio())
                .githubUsername(vo.getGithubUsername())
                .wechatQrcode(vo.getWechatQrcode())
                .build();
    }

    public List<DirectionLeaderDTO> toDirectionLeaderDTOs(List<MemberVO> leaders) {
        Map<Direction, MemberVO> leaderMap = leaders.stream()
                .collect(Collectors.toMap(MemberVO::getDirection, vo -> vo, (a, b) -> a));

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

    private DirectionLeaderDTO.LeaderInfo toLeaderInfo(MemberVO vo) {
        if (vo == null) {
            return null;
        }
        return DirectionLeaderDTO.LeaderInfo.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .nickname(vo.getNickname())
                .avatarFileId(vo.getAvatarFileId())
                .build();
    }
}
