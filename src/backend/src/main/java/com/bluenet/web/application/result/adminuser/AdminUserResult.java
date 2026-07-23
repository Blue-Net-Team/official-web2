package com.bluenet.web.application.result.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;

/**
 * 管理员用户管理结果对象
 */
public class AdminUserResult {

    private AdminUserResult() {
    }

    /**
     * 用户列表项
     */
    public record ListItem(
            Long id,
            String studentId,
            String username,
            String nickname,
            String email,
            Long roleId,
            String roleName,
            Direction direction,
            Long collegeId,
            String college,
            String major,
            Gender gender,
            String job,
            Boolean disable,
            Long avatarFileId,
            Integer assessmentGradeYear) {
    }

    /**
     * 创建用户结果
     */
    public record Created(
            Long id,
            String studentId,
            String username,
            Long roleId) {
    }

    /**
     * 用户详情
     */
    public record Detail(
            Long id,
            String studentId,
            String username,
            String nickname,
            String email,
            Long roleId,
            String roleName,
            Direction direction,
            Long collegeId,
            String college,
            String major,
            Gender gender,
            String job,
            Boolean disable,
            Long avatarFileId,
            String githubUsername,
            String bio,
            Integer assessmentGradeYear,
            Long experienceCount,
            Long achievementCount,
            Long answerCount,
            Long commentCount) {
    }
}
