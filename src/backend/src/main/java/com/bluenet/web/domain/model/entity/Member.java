package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员聚合根
 * <p>
 * 承载成员相关的业务规则和行为，作为团队成员信息的核心领域实体。 注意：Member 是只读聚合根，其数据来源于 User 及相关实体。
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

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
    private String roleName;
    private String bio;
    private String githubUsername;
    private Long wechatQrcode;
    private Integer enrollmentYear;
    private Integer assessmentGradeYear;

    private Member(Long id, String studentId, String username, String nickname,
            Direction direction, String job, Long avatarFileId, String college,
            String major, Gender gender, RoleType role, String roleName,
            String bio, String githubUsername, Long wechatQrcode,
            Integer enrollmentYear, Integer assessmentGradeYear) {
        this.id = id;
        this.studentId = studentId;
        this.username = username;
        this.nickname = nickname;
        this.direction = direction;
        this.job = job;
        this.avatarFileId = avatarFileId;
        this.college = college;
        this.major = major;
        this.gender = gender;
        this.role = role;
        this.roleName = roleName;
        this.bio = bio;
        this.githubUsername = githubUsername;
        this.wechatQrcode = wechatQrcode;
        this.enrollmentYear = enrollmentYear;
        this.assessmentGradeYear = assessmentGradeYear;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     */
    public static Member create(String studentId, String username, String nickname,
            Direction direction, String job, Long avatarFileId,
            String college, String major, Gender gender,
            RoleType role, String roleName, String bio,
            String githubUsername, Long wechatQrcode,
            Integer enrollmentYear, Integer assessmentGradeYear) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return new Member(null, studentId, username.trim(), nickname, direction, job,
                avatarFileId, college, major, gender, role, roleName, bio,
                githubUsername, wechatQrcode, enrollmentYear, assessmentGradeYear);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static Member reconstruct(Long id, String studentId, String username,
            String nickname, Direction direction, String job,
            Long avatarFileId, String college, String major,
            Gender gender, RoleType role, String roleName,
            String bio, String githubUsername, Long wechatQrcode,
            Integer enrollmentYear, Integer assessmentGradeYear) {
        return new Member(id, studentId, username, nickname, direction, job,
                avatarFileId, college, major, gender, role, roleName, bio,
                githubUsername, wechatQrcode, enrollmentYear, assessmentGradeYear);
    }

    /**
     * 判断是否为团队成员（角色至少为 MEMBER）
     */
    public boolean isTeamMember() {
        return this.role != null && this.role.isAtLeast(RoleType.MEMBER);
    }
}
