package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user")
public class UserDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String githubId;

    /**
     * 绑定的 GitHub 登录名。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String githubUsername;
    /**
     * 用户或报名使用的内部推荐码。
     */
    private String internalReferralCode;

    /**
     * 用户个人简介或补充说明。
     */
    private String bio;
}
