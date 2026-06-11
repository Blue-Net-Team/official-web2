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
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 学生学号。
     */
    private String studentId;
    /**
     * 用户真实姓名或登录用户名。
     */
    private String username;
    /**
     * 用户昵称或展示名。
     */
    private String nickname;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 成员在团队或组织中的岗位职责。
     */
    private String job;
    /**
     * 用户头像对应的文件记录标识。
     */
    private Long avatarFileId;
    /**
     * 学院名称展示值。
     */
    private String college;
    /**
     * 用户所在专业。
     */
    private String major;
    /**
     * 用户性别。
     */
    private Gender gender;
    /**
     * 用户角色展示值。
     */
    private RoleType role;
    /**
     * 用户个人简介或补充说明。
     */
    private String bio;
    /**
     * 绑定的 GitHub 登录名。
     */
    private String githubUsername;
    /**
     * 用户微信二维码文件ID。
     */
    private Long wechatQrcode;
    /**
     * 用户入学年份。
     */
    private Integer enrollmentYear;
    /**
     * 用于考核资格计算的年级年份，优先覆盖从学号推导出的入学年份。
     */
    private Integer assessmentGradeYear;
    /**
     * 用户角色名称。
     */
    private String roleName;
}
