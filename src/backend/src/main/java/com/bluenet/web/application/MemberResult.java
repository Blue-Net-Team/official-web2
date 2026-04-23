package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;

/**
 * 成员聚合的应用层结果对象。
 * <p>
 * 封装了成员相关操作返回给 API 层的数据。
 * </p>
 */
public record MemberResult(
        /** 唯一标识 */
        Long id,
        /** 学号 */
        String studentId,
        /** 用户名 */
        String username,
        /** 昵称 */
        String nickname,
        /** 方向 */
        Direction direction,
        /** 职位 */
        String job,
        /** 头像文件ID */
        Long avatarFileId,
        /** 学院 */
        String college,
        /** 专业 */
        String major,
        /** 性别 */
        Gender gender,
        /** 角色 */
        RoleType role,
        /** 角色名称 */
        String roleName,
        /** 个人简介 */
        String bio,
        /** GitHub用户名 */
        String githubUsername,
        /** 微信二维码 */
        String wechatQrcode,
        /** 入学年份 */
        Integer enrollmentYear,
        /** 考核年级 */
        Integer assessmentGradeYear) {
}
