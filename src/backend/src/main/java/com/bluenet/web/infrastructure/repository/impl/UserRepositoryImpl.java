package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.*;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import java.util.*;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.*;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.QrcodeVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.infrastructure.repository.mapper.*;
import com.bluenet.web.infrastructure.security.cache.PermissionCache;
import org.springframework.stereotype.Repository;

import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final QrcodeMapper qrcodeMapper;
    private final PermissionCache permissionCache;
    private final RoleMapper roleMapper;
    private final UserExperienceMapper userExperienceMapper;
    private final UserRepositoryConverter userConverter;
    private final CollegeRepositoryConverter collegeConverter;
    private final QrcodeRepositoryConverter qrcodeConverter;
    private final FileRepositoryConverter fileConverter;
    private final RoleRepositoryConverter roleConverter;

    /**
     * 保存新的用户 记录。
     *
     * @param user
     *            用户领域对象。
     */
    @Override
    public void save(User user) {
        UserDO dataObject = userConverter.toDataObject(user);
        userMapper.insert(dataObject);
        user.setId(dataObject.getId());
        log.info("save user {}", user);
    }

    /**
     * 按主键查询用户 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<UserVO> findById(Long id) {
        User user = userConverter.toEntity(userMapper.selectById(id));
        if (user == null) {
            log.warn("user not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    /**
     * 按邮箱查询用户视图。
     *
     * @param email
     *            邮箱地址，用于定位用户或验证码。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<UserVO> findByEmail(String email) {
        User user = userConverter.toEntity(userMapper.selectByEmail(email));
        if (user == null) {
            log.warn("user not found email {}", email);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    /**
     * 按学号查询用户或报名申请。
     *
     * @param studentId
     *            学生学号，用于定位用户或报名申请。
     * @return 查询到的用户 结果；不存在时为空。
     */
    @Override
    public Optional<UserVO> findByStudentId(String studentId) {
        User user = userConverter.toEntity(userMapper.selectByStudentId(studentId));
        if (user == null) {
            log.warn("user not found studentId {}", studentId);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    /**
     * 更新用户头像文件关联。
     *
     * @param user
     *            用户领域对象。
     * @param file
     *            文件领域对象或文件视图对象。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateAvatar(UserVO user, FileVO file) {
        if (file.getId() == null) {
            log.warn("更新头像的时候，file id 不能为空，请先保存文件获取id");
            throw new GlobalException("更新头像失败：文件ID不能为空");
        }

        return updateAvatar(user.getId(), file.getId());
    }

    /**
     * 更新用户头像文件关联。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param id
     *            业务记录主键。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateAvatar(Long userId, Long id) {
        int influence = userMapper.updateAvatarId(userId, id);
        if (influence == 0) {
            log.warn("更新文件失败，保存到数据库时没有影响任何行，userId {}, fileId {}", userId, id);
            throw new GlobalException("更新头像失败");
        }

        return influence;
    }

    /**
     * 更新用户微信二维码文件关联。
     *
     * @param user
     *            用户领域对象。
     * @param qrcode
     *            二维码领域对象或视图对象。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateQrcode(UserVO user, QrcodeVO qrcode) {
        if (qrcode.getId() == null) {
            log.warn("更新二维码的时候，qrcode id 不能为空，请先保存文件获取id");
            throw new GlobalException("更新二维码失败：qrcode ID不能为空");
        }
        int influence = userMapper.updateQrcodeId(user.getId(), qrcode.getId());

        if (influence == 0) {
            log.warn("更新二维码失败，保存到数据库时没有影响任何行，userId {}, qrcodeId {}", user.getId(), qrcode.getId());
            throw new GlobalException("更新二维码失败");
        }

        return influence;
    }

    /**
     * 更新用户个人资料字段。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param username
     *            用户姓名或登录名。
     * @param nickname
     *            用户昵称。
     * @param college
     *            学院名称。
     * @param major
     *            专业名称。
     * @param direction
     *            技术方向过滤条件。
     * @param gender
     *            性别。
     * @param bio
     *            个人简介。
     * @return 数据库受影响行数。
     */
    @Override
    public int updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio) {
        return userMapper.updateProfile(userId, username, nickname, college, major, direction, gender, bio);
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
    public Optional<UserVO> findByGithubId(String githubId) {
        User user = userConverter.toEntity(userMapper.selectByGithubId(githubId));
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
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
    public void clearGithubBinding(Long userId) {
        userMapper.clearGithubBinding(userId);
    }

    /**
     * 更新用户邮箱地址。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param newEmail
     *            新的邮箱地址。
     */
    @Override
    public void updateEmail(Long userId, String newEmail) {
        userMapper.updateEmail(userId, newEmail);
    }

    /**
     * 更新用户加密后的登录密码。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param encodedPassword
     *            加密后的密码。
     */
    @Override
    public void updatePassword(Long userId, String encodedPassword) {
        userMapper.updatePassword(userId, encodedPassword);
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

    /**
     * 在用户 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param user
     *            用户领域对象。
     * @return 转换后的目标模型对象。
     */
    private UserVO convertToVO(User user) {
        // 学院
        String collegeName = null;
        if (user.getCollegeId() != null) {
            College college = collegeConverter.toEntity(collegeMapper.selectById(user.getCollegeId()));
            collegeName = college != null ? college.getName() : null;
        } else {
            log.warn("user {} has no collegeId", user.getId());
        }

        // 微信二维码
        String wechatQrCodeUrl = null;
        if (user.getQrcodeId() != null) {
            Qrcode qrcode = qrcodeConverter.toEntity(qrcodeMapper.selectById(user.getQrcodeId()));
            if (qrcode != null && qrcode.getFileId() != null) {
                File wechatQrCode = fileConverter.toEntity(fileMapper.selectById(qrcode.getFileId()));
                wechatQrCodeUrl = wechatQrCode != null ? wechatQrCode.getUrl() : null;
            }
        } else {
            log.warn("user {} has no qrcodeId", user.getId());
        }

        // 权限列表
        Set<String> permissions = new HashSet<>();
        if (user.getRoleId() != null) {
            permissions = permissionCache.getPermissionsByRole(user.getRoleId());
        } else {
            log.warn("user {} has no roleId", user.getId());
        }

        // 角色
        String roleName;
        if (user.getRoleId() != null) {
            Role roleOptional = roleConverter.toEntity(roleMapper.selectById(user.getRoleId()));
            roleName = roleOptional != null ? roleOptional.getName() : null;
        } else {
            log.warn("user {} has no roleId", user.getId());
            roleName = null;
        }

        return UserVO.builder()
                .id(user.getId())
                .roleName(roleName)
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .college(collegeName)
                .password(user.getPassword())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .assessmentGradeYear(user.getAssessmentGradeYear())
                .job(user.getJob())
                .direction(user.getDirection())
                .gender(user.getGender())
                .major(user.getMajor())
                .wechatQrcode(wechatQrCodeUrl)
                .avatarFileId(user.getAvatarId())
                .githubUsername(user.getGithubUsername())
                .bio(user.getBio())
                .disabled(user.getDisable())
                .permissions(permissions)
                .build();
    }
}
