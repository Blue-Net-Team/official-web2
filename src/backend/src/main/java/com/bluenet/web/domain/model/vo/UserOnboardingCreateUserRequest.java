package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import lombok.Builder;

/**
 * 用户入职创建用户请求值对象。
 */
@Builder
public record UserOnboardingCreateUserRequest(
        /** 学号 */
        String studentId,
        /** 邮箱 */
        String email,
        /** 角色 ID */
        Long roleId,
        /** 姓名 */
        String username,
        /** 学院 ID */
        Long collegeId,
        /** 专业 */
        String major,
        /** 考核年级 */
        Integer assessmentGradeYear,
        /** 方向 */
        Direction direction,
        /** 性别 */
        Gender gender,
        /** 头像文件 ID */
        Long avatarId) {
}
