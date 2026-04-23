package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报名聚合根
 * <p>
 * 承载报名相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Enroll {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 用户真实姓名或登录用户名。
     */
    private String username;
    /**
     * 学生学号。
     */
    private String studentId;
    /**
     * 用户密码哈希值或报名初始密码。
     */
    private String password;
    /**
     * 用户或报名使用的内部推荐码。
     */
    private String internalReferralCode;
    /**
     * 用户、报名或统计记录所属学院标识。
     */
    private Long collegeId;
    /**
     * 用户所在专业。
     */
    private String major;
    /**
     * 用户性别。
     */
    private Gender gender;
    /**
     * 用户或考核所属技术方向。
     */
    private Direction direction;
    /**
     * 用户头像文件标识。
     */
    private Long avatarId;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private EnrollStatus status;
    /**
     * 用户邮箱地址。
     */
    private String email;
    /**
     * 报名自我介绍或申请说明。
     */
    private String introduction;
    /**
     * 学院名称（展示用）。
     */
    private String collegeName;
    /**
     * 推荐人的用户标识。
     */
    private Long referralUserId;
    /**
     * 推荐人的展示名称。
     */
    private String referralUserName;

    private Enroll(Long id, String username, String studentId, String password, String internalReferralCode,
            Long collegeId, String major, Gender gender, Direction direction, Long avatarId,
            EnrollStatus status, String email, String introduction, String collegeName,
            Long referralUserId, String referralUserName) {
        this.id = id;
        this.username = username;
        this.studentId = studentId;
        this.password = password;
        this.internalReferralCode = internalReferralCode;
        this.collegeId = collegeId;
        this.major = major;
        this.gender = gender;
        this.direction = direction;
        this.avatarId = avatarId;
        this.status = status;
        this.email = email;
        this.introduction = introduction;
        this.collegeName = collegeName;
        this.referralUserId = referralUserId;
        this.referralUserName = referralUserName;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     */
    public static Enroll create(String username, String studentId, String password,
            String internalReferralCode, Long collegeId, String major,
            Gender gender, Direction direction, Long avatarId,
            String email, String introduction) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return new Enroll(null, username.trim(), studentId, password, internalReferralCode,
                collegeId, major, gender, direction, avatarId, EnrollStatus.PENDING,
                email, introduction, null, null, null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static Enroll reconstruct(Long id, String username, String studentId, String password,
            String internalReferralCode, Long collegeId, String major,
            Gender gender, Direction direction, Long avatarId,
            EnrollStatus status, String email, String introduction,
            String collegeName, Long referralUserId, String referralUserName) {
        return new Enroll(id, username, studentId, password, internalReferralCode, collegeId, major,
                gender, direction, avatarId, status, email, introduction, collegeName,
                referralUserId, referralUserName);
    }

    /**
     * 更新报名信息 —— 带领域校验
     */
    public void updateInfo(String username, String studentId, Long collegeId, String major,
            Gender gender, Direction direction, Long avatarId, String email,
            String introduction, String internalReferralCode, String password) {
        if (this.status != EnrollStatus.PENDING) {
            throw new IllegalArgumentException("报名已审核，无法更新报名信息");
        }
        this.username = username;
        this.studentId = studentId;
        this.collegeId = collegeId;
        this.major = major;
        this.gender = gender;
        this.direction = direction;
        this.avatarId = avatarId;
        this.email = email;
        this.introduction = introduction;
        this.internalReferralCode = internalReferralCode;
        this.password = password;
    }

    /**
     * 审核通过 —— 带领域校验
     */
    public void approve() {
        if (this.status != EnrollStatus.PENDING) {
            throw new IllegalArgumentException("只能审核待审核状态的报名");
        }
        this.status = EnrollStatus.APPROVED;
    }

    /**
     * 审核拒绝 —— 带领域校验
     */
    public void reject() {
        if (this.status != EnrollStatus.PENDING) {
            throw new IllegalArgumentException("只能审核待审核状态的报名");
        }
        this.status = EnrollStatus.REJECTED;
    }
}
