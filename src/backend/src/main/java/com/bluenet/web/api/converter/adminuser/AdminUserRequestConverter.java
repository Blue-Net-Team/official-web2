package com.bluenet.web.api.converter.adminuser;

import com.bluenet.web.api.dto.adminuser.*;
import com.bluenet.web.application.command.adminuser.AdminUserCommands;
import org.springframework.stereotype.Component;

@Component
public class AdminUserRequestConverter {

    public AdminUserCommands.GetUserListCommand toCommand(AdminUserListQueryDTO dto) {
        return new AdminUserCommands.GetUserListCommand(
                dto.getPage(), dto.getSize(), dto.getRoleId(),
                dto.getDirection(), dto.getCollegeId(), dto.getKeyword());
    }

    public AdminUserCommands.UpdateUserCommand toCommand(Long userId, AdminUserUpdateRequestDTO dto) {
        return new AdminUserCommands.UpdateUserCommand(
                userId, dto.getRoleId(), dto.getDirection(), dto.getDisable(), dto.getJob());
    }

    public AdminUserCommands.ResetPasswordCommand toCommand(Long userId, AdminUserResetPasswordRequestDTO dto) {
        return new AdminUserCommands.ResetPasswordCommand(
                userId, dto.getNewPassword(), dto.getConfirmPassword());
    }

    public AdminUserCommands.BatchOperateCommand toCommand(AdminUserBatchOperateRequestDTO dto) {
        return new AdminUserCommands.BatchOperateCommand(dto.getUserIds());
    }

    public AdminUserCommands.BatchUpdateRoleCommand toCommand(AdminUserBatchUpdateRoleRequestDTO dto) {
        return new AdminUserCommands.BatchUpdateRoleCommand(dto.getUserIds(), dto.getRoleId());
    }

    public AdminUserCommands.CreateUserCommand toCommand(AdminUserCreateRequestDTO dto) {
        return new AdminUserCommands.CreateUserCommand(
                dto.getStudentId(),
                dto.getEmail(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getNickname(),
                dto.getRoleId(),
                dto.getCollegeId(),
                dto.getMajor(),
                dto.getDirection(),
                dto.getGender(),
                dto.getJob(),
                dto.getAssessmentGradeYear());
    }
}
