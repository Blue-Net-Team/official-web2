package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 学生学号。
     */
    private String studentId;
    /**
     * 用户邮箱地址。
     */
    private String email;
    /**
     * 用户绑定的角色标识。
     */
    private Long roleId;
    /**
     * 用户密码哈希值或报名初始密码。
     */
    private String password;
    /**
     * 用户真实姓名或登录用户名。
     */
    private String username;
    /**
     * 用户昵称或展示名。
     */
    private String nickname;
    /**
     * 用户、报名或统计记录所属学院标识。
     */
    private Long collegeId;
    /**
     * 用户所在专业。
     */
    private String major;
    /**
     * 用于考核资格计算的年级年份，优先覆盖从学号推导出的入学年份。
     */
    private Integer assessmentGradeYear;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 用户性别。
     */
    private Gender gender;
    /**
     * 成员在团队或组织中的岗位职责。
     */
    private String job;
    /**
     * 用户头像文件标识。
     */
    private Long avatarId;
    /**
     * 用户账号是否被禁用。
     */
    private Boolean disable;
    /**
     * 用户微信二维码文件关联标识。
     */
    private Long qrcodeId;
    /**
     * 绑定的 GitHub 用户唯一标识。
     */
    private String githubId;
    /**
     * 绑定的 GitHub 登录名。
     */
    private String githubUsername;
    /**
     * 用户或报名使用的内部推荐码。
     */
    private String internalReferralCode;
    /**
     * 用户个人简介或补充说明。
     */
    private String bio;

    public void changePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        this.password = encodedPassword;
    }

    /**
     * 管理员重置用户密码（需二次确认）
     *
     * @param newPassword
     *            新密码
     * @param confirmPassword
     *            确认密码
     */
    public void resetPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        this.password = newPassword;
    }

    /**
     * 管理员更新用户字段（仅更新非 null 的字段）
     *
     * @param roleId
     *            角色标识
     * @param direction
     *            技术方向
     * @param disable
     *            禁用状态
     * @param job
     *            岗位职责
     * @param studentId
     *            学生学号
     * @param email
     *            邮箱地址
     * @param username
     *            用户名
     * @param nickname
     *            昵称
     * @param collegeId
     *            学院标识
     * @param major
     *            专业
     * @param gender
     *            性别
     * @param assessmentGradeYear
     *            考核年级年份
     */
    public void updateAdminFields(Long roleId, Direction direction, Boolean disable, String job,
            String studentId, String email, String username, String nickname,
            Long collegeId, String major, Gender gender, Integer assessmentGradeYear) {
        if (roleId != null) {
            this.roleId = roleId;
        }
        if (direction != null) {
            this.direction = direction;
        }
        if (disable != null) {
            this.disable = disable;
        }
        if (job != null) {
            this.job = job;
        }
        if (studentId != null) {
            this.studentId = studentId;
        }
        if (email != null) {
            this.email = email;
        }
        if (username != null) {
            this.username = username;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (collegeId != null) {
            this.collegeId = collegeId;
        }
        if (major != null) {
            this.major = major;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (assessmentGradeYear != null) {
            this.assessmentGradeYear = assessmentGradeYear;
        }
    }

    /**
     * 构造新用户聚合根
     *
     * @param studentId
     *            学生学号
     * @param email
     *            邮箱地址
     * @param roleId
     *            角色标识
     * @param password
     *            密码
     * @param username
     *            用户名
     * @param nickname
     *            昵称
     * @param collegeId
     *            学院标识
     * @param major
     *            专业
     * @param assessmentGradeYear
     *            考核年级年份
     * @param direction
     *            技术方向
     * @param gender
     *            性别
     * @param job
     *            岗位
     * @param avatarId
     *            头像文件标识
     * @param qrcodeId
     *            二维码文件标识
     * @param githubId
     *            GitHub 用户标识
     * @param githubUsername
     *            GitHub 登录名
     * @param internalReferralCode
     *            内部推荐码
     * @param bio
     *            个人简介
     * @return 新的用户实体
     */
    public static User create(String studentId, String email, Long roleId, String password,
            String username, String nickname, Long collegeId, String major,
            Integer assessmentGradeYear, Direction direction, Gender gender,
            String job, Long avatarId, Long qrcodeId, String githubId,
            String githubUsername, String internalReferralCode, String bio) {
        User user = new User();
        user.setStudentId(studentId);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setPassword(password);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setCollegeId(collegeId);
        user.setMajor(major);
        user.setAssessmentGradeYear(assessmentGradeYear);
        user.setDirection(direction);
        user.setGender(gender);
        user.setJob(job);
        user.setAvatarId(avatarId);
        user.setDisable(false);
        user.setQrcodeId(qrcodeId);
        user.setGithubId(githubId);
        user.setGithubUsername(githubUsername);
        user.setInternalReferralCode(internalReferralCode);
        user.setBio(bio);
        return user;
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            用户ID
     * @param studentId
     *            学生学号
     * @param email
     *            邮箱地址
     * @param roleId
     *            角色标识
     * @param password
     *            密码
     * @param username
     *            用户名
     * @param nickname
     *            昵称
     * @param collegeId
     *            学院标识
     * @param major
     *            专业
     * @param assessmentGradeYear
     *            考核年级年份
     * @param direction
     *            技术方向
     * @param gender
     *            性别
     * @param job
     *            岗位
     * @param avatarId
     *            头像文件标识
     * @param disable
     *            是否禁用
     * @param qrcodeId
     *            二维码文件标识
     * @param githubId
     *            GitHub 用户标识
     * @param githubUsername
     *            GitHub 登录名
     * @param internalReferralCode
     *            内部推荐码
     * @param bio
     *            个人简介
     * @return 重建的用户实体
     */
    public static User reconstruct(Long id, String studentId, String email, Long roleId, String password,
            String username, String nickname, Long collegeId, String major,
            Integer assessmentGradeYear, Direction direction, Gender gender,
            String job, Long avatarId, Boolean disable, Long qrcodeId,
            String githubId, String githubUsername, String internalReferralCode,
            String bio) {
        User user = new User();
        user.setId(id);
        user.setStudentId(studentId);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setPassword(password);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setCollegeId(collegeId);
        user.setMajor(major);
        user.setAssessmentGradeYear(assessmentGradeYear);
        user.setDirection(direction);
        user.setGender(gender);
        user.setJob(job);
        user.setAvatarId(avatarId);
        user.setDisable(disable);
        user.setQrcodeId(qrcodeId);
        user.setGithubId(githubId);
        user.setGithubUsername(githubUsername);
        user.setInternalReferralCode(internalReferralCode);
        user.setBio(bio);
        return user;
    }

    public static User reconstruct(Long id, String password) {
        User user = new User();
        user.setId(id);
        user.setPassword(password);
        return user;
    }
}
