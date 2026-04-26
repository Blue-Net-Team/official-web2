package com.bluenet.web.application;

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
            String college,
            String major,
            Gender gender,
            String job,
            Boolean disable,
            Long avatarFileId) {
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
