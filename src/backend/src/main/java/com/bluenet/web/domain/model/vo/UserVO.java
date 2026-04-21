package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * 用户领域值对象
 * <p>
 * 封装用户相关的领域数据，用于在领域层传递用户信息。 不包含技术实现细节（如数据库ID、外键等）。
 * </p>
 */
@Getter
@AllArgsConstructor
@Builder
public class UserVO {
    /**
     * 用户ID（数据库自增ID）
     */
    private Long id;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 密码（已哈希）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String username;

    /**
     * 用户名/昵称
     */
    private String nickname;

    /**
     * 学院
     */
    private String college;

    /**
     * 专业
     */
    private String major;
    /**
     * 用于考核资格计算的年级年份，优先覆盖从学号推导出的入学年份。
     */
    private Integer assessmentGradeYear;

    /**
     * 方向
     */
    private Direction direction;

    /**
     * 性别
     */
    private Gender gender;

    /**
     * 职责
     */
    private String job;

    /**
     * 头像文件ID
     */
    private Long avatarFileId;

    /**
     * 微信二维码
     */
    private String wechatQrcode;

    /**
     * GitHub用户名
     */
    private String githubUsername;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 是否禁用
     */
    private boolean disabled;

    /**
     * 用户权限列表
     */
    private Set<String> permissions;
}
