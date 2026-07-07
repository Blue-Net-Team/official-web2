package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.*;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import java.time.LocalDateTime;
import java.util.*;

import com.bluenet.web.domain.model.entity.*;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluenet.web.infrastructure.repository.dataobject.*;
import com.bluenet.web.infrastructure.repository.mapper.*;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserRepositoryImpl extends ServiceImpl<UserMapper, UserDO> implements UserRepository {
    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final QrcodeMapper qrcodeMapper;
    private final PermissionCache permissionCache;
    private final RoleMapper roleMapper;
    private final UserExperienceMapper userExperienceMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final AssessmentAnswerMapper assessmentAnswerMapper;
    private final AssessmentSessionMapper assessmentSessionMapper;
    private final CommentMapper commentMapper;
    private final UserRepositoryConverter userConverter;
    private final CollegeRepositoryConverter collegeConverter;
    private final QrcodeRepositoryConverter qrcodeConverter;
    private final FileRepositoryConverter fileConverter;
    private final RoleRepositoryConverter roleConverter;
    private final FileRepository fileRepository;

    /**
     * 保存或更新用户记录。
     *
     * @param user
     *            用户领域对象。若 id 为空则插入，否则按 id 更新。
     */
    @Override
    @Transactional
    public void save(User user) {
        UserDO dataObject = userConverter.toDataObject(user);
        saveOrUpdate(dataObject);
        user.setId(dataObject.getId());
        log.info("save user {}", user);
    }

    /**
     * 批量保存或更新用户记录。
     *
     * @param users
     *            用户领域对象列表。每个对象若 id 为空则插入，否则按 id 更新。
     */
    @Override
    @Transactional
    public void saveAll(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<UserDO> toInsert = new ArrayList<>();
        List<User> insertEntities = new ArrayList<>();
        List<UserDO> toUpdate = new ArrayList<>();
        for (User user : users) {
            UserDO dataObject = userConverter.toDataObject(user);
            if (dataObject.getId() == null) {
                toInsert.add(dataObject);
                insertEntities.add(user);
            } else {
                toUpdate.add(dataObject);
            }
        }
        if (!toInsert.isEmpty()) {
            saveBatch(toInsert);
            for (int i = 0; i < toInsert.size(); i++) {
                insertEntities.get(i).setId(toInsert.get(i).getId());
            }
        }
        if (!toUpdate.isEmpty()) {
            updateBatchById(toUpdate);
        }
        log.info("batch saved {} users", users.size());
    }

    /**
     * 按主键查询用户 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userConverter.toEntity(getById(id)));
    }

    /**
     * 按邮箱查询用户视图。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userConverter.toEntity(userMapper.selectByEmail(email)));
    }

    /**
     * 按学号查询用户或报名申请。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<User> findByStudentId(String studentId) {
        return Optional.ofNullable(userConverter.toEntity(userMapper.selectByStudentId(studentId)));
    }

    /**
     * 统计用户主页各标签页展示数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 查询或处理得到的用户 结果。
     */
    @Override
    public TabCountsVO getTabCounts(Long userId) {
        int projects = Math.toIntExact(userExperienceMapper.countByUserIdAndType(userId, ExperienceType.PROJECT));
        int competitions = Math
                .toIntExact(userExperienceMapper.countByUserIdAndType(userId, ExperienceType.COMPETITION));
        int internships = Math.toIntExact(userExperienceMapper.countByUserIdAndType(userId, ExperienceType.INTERNSHIP));

        return new TabCountsVO(projects, competitions, internships);
    }

    /**
     * 按 GitHub 用户标识查询已绑定用户。
     *
     * @param githubId
     *            GitHub 用户唯一标识。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<User> findByGithubId(String githubId) {
        return Optional.ofNullable(userConverter.toEntity(userMapper.selectByGithubId(githubId)));
    }

    /**
     * 保存用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param githubId
     *            GitHub 用户唯一标识。
     * @param githubUsername
     *            GitHub 登录名。
     */
    @Override
    @Transactional
    public void updateGithubBinding(Long userId, String githubId, String githubUsername) {
        userMapper.updateGithubBinding(userId, githubId, githubUsername);
    }

    /**
     * 清除用户与 GitHub 账号的绑定信息。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     */
    @Override
    @Transactional
    public void clearGithubBinding(Long userId) {
        userMapper.clearGithubBinding(userId);
    }

    /**
     * 判断内部推荐码是否已被用户占用。
     *
     * @param code
     *            验证码或推荐码。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByInternalReferralCode(String code) {
        return userMapper.selectByInternalReferralCode(code) != null;
    }

    // ========== Admin User Management ==========

    @Override
    public Page<User> findPage(Pageable pageable, Long roleId, Direction direction, Long collegeId, String keyword) {
        QueryWrapper<UserDO> wrapper = new QueryWrapper<>();
        if (roleId != null) {
            wrapper.eq("role_id", roleId);
        }
        if (direction != null) {
            wrapper.eq("direction", direction.name().toLowerCase());
        }
        if (collegeId != null) {
            wrapper.eq("college_id", collegeId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("student_id", keyword).or().like("username", keyword));
        }
        wrapper.orderByDesc("id");

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDO> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());
        mpPage = userMapper.selectPage(mpPage, wrapper);

        List<User> users = mpPage.getRecords()
                .stream()
                .map(userConverter::toEntity)
                .toList();
        return new PageImpl<>(users, pageable, mpPage.getTotal());
    }

    @Override
    @Transactional
    public void deleteByIdWithCascade(Long userId) {
        // 0. 获取用户，提取头像文件ID
        UserDO userDO = userMapper.selectById(userId);
        Long avatarId = userDO != null ? userDO.getAvatarId() : null;

        // 1. 删除用户经历
        userExperienceMapper.delete(new QueryWrapper<UserExperienceDO>().eq("user_id", userId));
        // 2. 删除用户成就关联
        userAchievementMapper.delete(new QueryWrapper<UserAchievementDO>().eq("user_id", userId));
        // 3. 删除考核答案
        assessmentAnswerMapper.delete(new QueryWrapper<AssessmentAnswerDO>().eq("user_id", userId));
        // 4. 删除考核会话
        assessmentSessionMapper.delete(new QueryWrapper<AssessmentSessionDO>().eq("user_id", userId));
        // 5. 删除评论
        commentMapper.delete(new QueryWrapper<CommentDO>().eq("user_id", userId));
        // 6. 删除用户
        userMapper.deleteById(userId);
        // 7. 删除头像文件
        if (avatarId != null) {
            try {
                fileRepository.deleteFileById(avatarId);
                log.info("Deleted avatar file {} for user {}", avatarId, userId);
            } catch (Exception e) {
                log.warn("Failed to delete avatar file {} for user {}: {}", avatarId, userId, e.getMessage());
            }
        }
        log.info("Cascade deleted user {}", userId);
    }

    @Override
    public List<Long> findUserIdsToDisableByElimination(LocalDateTime cutoffTime) {
        return userMapper.selectUserIdsToDisableByElimination(cutoffTime);
    }

    @Override
    public UserStatistics getStatistics(Long userId) {
        long expCount = userExperienceMapper.selectCount(new QueryWrapper<UserExperienceDO>().eq("user_id", userId));
        long achCount = userAchievementMapper.selectCount(new QueryWrapper<UserAchievementDO>().eq("user_id", userId));
        long ansCount = assessmentAnswerMapper
                .selectCount(new QueryWrapper<AssessmentAnswerDO>().eq("user_id", userId));
        long cmtCount = commentMapper.selectCount(new QueryWrapper<CommentDO>().eq("user_id", userId));
        return new UserStatistics(expCount, achCount, ansCount, cmtCount);
    }

}
