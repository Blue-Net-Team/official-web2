package com.bluenet.web.application.result.user;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;

/**
 * 用户信息聚合的应用层结果对象。
 * <p>
 * 封装了用户信息相关操作返回给 API 层的数据。
 * </p>
 */
public record UserInfoResult(
        /** 唯一标识 */
        Long id,
        /** 用户名 */
        String username,
        /** 昵称 */
        String nickname,
        /** 学院 */
        String college,
        /** 专业 */
        String major,
        /** 年级 */
        String grade,
        /** 邮箱 */
        String email,
        /** 头像文件ID */
        Long avatarFileId,
        /** 角色名称 */
        String roleName,
        /** 方向 */
        Direction direction,
        /** 性别 */
        Gender gender,
        /** 个人简介 */
        String bio,
        /** GitHub用户名 */
        String githubUsername,
        /** 微信二维码文件ID */
        Long wechatQrcode) {
}
