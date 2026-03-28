package com.bluenet.web.infrastructure.repository.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Override
    public void save(User user) {
        userMapper.insert(user);
        log.info("save user {}", user);
    }

    @Override
    public Optional<UserVO> findById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("user not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    @Override
    public Optional<UserVO> findByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            log.warn("user not found email {}", email);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    @Override
    public Optional<UserVO> findByStudentId(String studentId) {
        User user = userMapper.selectByStudentId(studentId);
        if (user == null) {
            log.warn("user not found studentId {}", studentId);
            return Optional.empty();
        }
        return Optional.of(convertToVO(user));
    }

    @Override
    public int updateAvatar(UserVO user, FileVO file) {
        if (file.getId() == null) {
            log.warn("更新头像的时候，file id 不能为空，请先保存文件获取id");
            throw new GlobalException("更新头像失败：文件ID不能为空");
        }

        return updateAvatar(user.getId(), file.getId());
    }

    @Override
    public int updateAvatar(Long userId, Long id) {
        int influence = userMapper.updateAvatarId(userId, id);
        if (influence == 0) {
            log.warn("更新文件失败，保存到数据库时没有影响任何行，userId {}, fileId {}", userId, id);
            throw new GlobalException("更新头像失败");
        }

        return influence;
    }

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

    @Override
    public int updateProfile(Long userId, String username, String nickname, String college,
            String major, Direction direction, Gender gender, String bio) {
        return userMapper.updateProfile(userId, username, nickname, college, major, direction, gender, bio);
    }

    @Override
    public TabCountsVO getTabCounts(Long userId) {
        int projects = Math.toIntExact(
                userExperienceMapper.selectCount(
                        new LambdaQueryWrapper<UserExperience>()
                                .eq(UserExperience::getUserId, userId)
                                .eq(UserExperience::getType, ExperienceType.PROJECT)));
        int competitions = Math.toIntExact(
                userExperienceMapper.selectCount(
                        new LambdaQueryWrapper<UserExperience>()
                                .eq(UserExperience::getUserId, userId)
                                .eq(UserExperience::getType, ExperienceType.COMPETITION)));
        int internships = Math.toIntExact(
                userExperienceMapper.selectCount(
                        new LambdaQueryWrapper<UserExperience>()
                                .eq(UserExperience::getUserId, userId)
                                .eq(UserExperience::getType, ExperienceType.INTERNSHIP)));

        return new TabCountsVO(projects, competitions, internships);
    }

    private UserVO convertToVO(User user) {
        // 学院
        String collegeName = null;
        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            collegeName = college != null ? college.getName() : null;
        } else {
            log.warn("user {} has no collegeId", user.getId());
        }

        // 微信二维码
        String wechatQrCodeUrl = null;
        if (user.getQrcodeId() != null) {
            Qrcode qrcode = qrcodeMapper.selectById(user.getQrcodeId());
            if (qrcode != null && qrcode.getFileId() != null) {
                File wechatQrCode = fileMapper.selectById(qrcode.getFileId());
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
            Role roleOptional = roleMapper.selectById(user.getRoleId());
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
