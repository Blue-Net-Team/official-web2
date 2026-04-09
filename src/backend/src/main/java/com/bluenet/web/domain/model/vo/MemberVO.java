package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberVO {
    private Long id;
    private String studentId;
    private String username;
    private String nickname;
    private Direction direction;
    private String job;
    private Long avatarFileId;
    private String college;
    private String major;
    private Gender gender;
    private RoleType role;
    private String bio;
    private String githubUsername;
    private String wechatQrcode;
    private Integer enrollmentYear;
    private String roleName;
}
