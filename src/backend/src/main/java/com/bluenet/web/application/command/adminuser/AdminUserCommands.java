package com.bluenet.web.application.command.adminuser;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;

import java.util.List;

/**
 * 管理员用户管理命令对象集合
 */
public class AdminUserCommands {

    private AdminUserCommands() {
    }

    /**
     * 分页查询用户列表命令
     */
    public record GetUserListCommand(
            Integer page,
            Integer size,
            Long roleId,
            Direction direction,
            Long collegeId,
            String keyword) {
    }

    /**
     * 更新用户信息命令
     */
    public record UpdateUserCommand(
            Long userId,
            Long roleId,
            Direction direction,
            Boolean disable,
            String job,
            String studentId,
            String email,
            String username,
            String nickname,
            Long collegeId,
            String major,
            Gender gender,
            Integer assessmentGradeYear) {
    }

    /**
     * 重置密码命令
     */
    public record ResetPasswordCommand(
            Long userId,
            String newPassword,
            String confirmPassword) {
    }

    /**
     * 批量操作用户命令
     */
    public record BatchOperateCommand(
            List<Long> userIds) {
    }

    /**
     * 批量更新角色命令
     */
    public record BatchUpdateRoleCommand(
            List<Long> userIds,
            Long roleId) {
    }

    /**
     * 创建用户命令
     */
    public record CreateUserCommand(
            String studentId,
            String email,
            String username,
            String password,
            String nickname,
            Long roleId,
            Long collegeId,
            String major,
            Direction direction,
            Gender gender,
            String job,
            Integer assessmentGradeYear) {
    }
}
