package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AdminUserResult;
import com.bluenet.web.application.command.adminuser.AdminUserCommands;
import com.bluenet.web.application.service.AdminUserAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserAppServiceImpl implements AdminUserAppService {

    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_BATCH_SIZE = 50;

    @Override
    public Page<AdminUserResult.ListItem> getUserList(AdminUserCommands.GetUserListCommand command) {
        Pageable pageable = PageRequest.of(
                command.page() != null ? command.page() : 0,
                command.size() != null ? command.size() : 20);
        Page<User> userPage = userRepository.findPage(
                pageable,
                command.roleId(),
                command.direction(),
                command.collegeId(),
                command.keyword());
        return userPage.map(this::toListItem);
    }

    @Override
    public AdminUserResult.Detail getUserDetail(Long userId) {
        User user = userRepository.findEntityById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        UserRepository.UserStatistics stats = userRepository.getStatistics(userId);
        return toDetail(user, stats);
    }

    @Override
    @Transactional
    public void updateUser(AdminUserCommands.UpdateUserCommand command) {
        User user = userRepository.findEntityById(command.userId())
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        user.updateAdminFields(command.roleId(), command.direction(), command.disable(), command.job());
        userRepository.updateAdminFields(
                command.userId(),
                command.roleId(),
                command.direction(),
                command.disable(),
                command.job());
    }

    @Override
    @Transactional
    public void resetPassword(AdminUserCommands.ResetPasswordCommand command) {
        User user = userRepository.findEntityById(command.userId())
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        user.resetPassword(command.newPassword(), command.confirmPassword());
        userRepository.updatePassword(command.userId(), passwordEncoder.encode(user.getPassword()));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findEntityById(userId)
                .orElseThrow(() -> new DataNotFound("用户不存在"));
        preventSuperAdminModification(user);
        userRepository.deleteByIdWithCascade(userId);
    }

    @Override
    @Transactional
    public void batchDelete(AdminUserCommands.BatchOperateCommand command) {
        validateBatchSize(command.userIds());
        for (Long userId : command.userIds()) {
            User user = userRepository.findEntityById(userId).orElse(null);
            if (user != null) {
                preventSuperAdminModification(user);
            }
        }
        userRepository.batchDeleteByIds(command.userIds());
    }

    @Override
    @Transactional
    public void batchDisable(AdminUserCommands.BatchOperateCommand command, Boolean disable) {
        validateBatchSize(command.userIds());
        for (Long userId : command.userIds()) {
            User user = userRepository.findEntityById(userId).orElse(null);
            if (user != null) {
                preventSuperAdminModification(user);
            }
        }
        userRepository.batchUpdateDisable(command.userIds(), disable);
    }

    @Override
    @Transactional
    public void batchUpdateRole(AdminUserCommands.BatchUpdateRoleCommand command) {
        validateBatchSize(command.userIds());
        userRepository.batchUpdateRole(command.userIds(), command.roleId());
    }

    private void validateBatchSize(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BadRequest("用户ID列表不能为空");
        }
        if (userIds.size() > MAX_BATCH_SIZE) {
            throw new BadRequest("批量操作最多支持 " + MAX_BATCH_SIZE + " 个用户");
        }
    }

    private void preventSuperAdminModification(User user) {
        RoleDO role = roleMapper.selectById(user.getRoleId());
        if (role != null && RoleType.SUPER_ADMIN.getName().equals(role.getName())) {
            throw new BadRequest("不能对超级管理员执行此操作");
        }
    }

    private AdminUserResult.ListItem toListItem(User user) {
        String roleName = null;
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            roleName = role != null ? role.getName() : null;
        }
        String collegeName = null;
        if (user.getCollegeId() != null) {
            collegeName = collegeRepository.findById(user.getCollegeId())
                    .map(College::getName)
                    .orElse(null);
        }
        return new AdminUserResult.ListItem(
                user.getId(), user.getStudentId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getRoleId(), roleName, user.getDirection(),
                collegeName, user.getMajor(), user.getGender(), user.getJob(),
                user.getDisable(), user.getAvatarId());
    }

    private AdminUserResult.Detail toDetail(User user, UserRepository.UserStatistics stats) {
        String roleName = null;
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            roleName = role != null ? role.getName() : null;
        }
        String collegeName = null;
        if (user.getCollegeId() != null) {
            collegeName = collegeRepository.findById(user.getCollegeId())
                    .map(College::getName)
                    .orElse(null);
        }
        return new AdminUserResult.Detail(
                user.getId(), user.getStudentId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getRoleId(), roleName, user.getDirection(),
                collegeName, user.getMajor(), user.getGender(), user.getJob(),
                user.getDisable(), user.getAvatarId(), user.getGithubUsername(),
                user.getBio(), user.getAssessmentGradeYear(),
                stats.experienceCount(), stats.achievementCount(),
                stats.answerCount(), stats.commentCount());
    }
}
