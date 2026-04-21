package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
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
}
