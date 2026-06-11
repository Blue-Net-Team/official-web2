package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Member;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.infrastructure.repository.converter.CollegeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.converter.FileRepositoryConverter;
import com.bluenet.web.infrastructure.repository.converter.MemberRepositoryConverter;
import com.bluenet.web.infrastructure.repository.converter.QrcodeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.RoleDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MemberRepositoryImpl implements MemberRepository {
    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final QrcodeMapper qrcodeMapper;
    private final RoleMapper roleMapper;
    private final MemberRepositoryConverter converter;
    private final CollegeRepositoryConverter collegeConverter;
    private final QrcodeRepositoryConverter qrcodeConverter;
    private final FileRepositoryConverter fileConverter;
    private final String systemUsername;

    public MemberRepositoryImpl(
            UserMapper userMapper,
            CollegeMapper collegeMapper,
            FileMapper fileMapper,
            QrcodeMapper qrcodeMapper,
            RoleMapper roleMapper,
            MemberRepositoryConverter converter,
            CollegeRepositoryConverter collegeConverter,
            QrcodeRepositoryConverter qrcodeConverter,
            FileRepositoryConverter fileConverter,
            @Value("${system-user.username}") String systemUsername) {
        this.userMapper = userMapper;
        this.collegeMapper = collegeMapper;
        this.fileMapper = fileMapper;
        this.qrcodeMapper = qrcodeMapper;
        this.roleMapper = roleMapper;
        this.converter = converter;
        this.collegeConverter = collegeConverter;
        this.qrcodeConverter = qrcodeConverter;
        this.fileConverter = fileConverter;
        this.systemUsername = systemUsername;
    }

    @Override
    public Page<Member> findAll(Direction direction, Pageable pageable) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDO> mybatisPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageable.getPageNumber() + 1, pageable.getPageSize());

        List<String> roleNames = List.of(
                RoleType.MEMBER.getName(),
                RoleType.DIRECTION_ADMIN.getName(),
                RoleType.SUPER_ADMIN.getName());

        com.baomidou.mybatisplus.core.metadata.IPage<UserDO> result = userMapper.selectByRoleNamesAndDirection(
                mybatisPage,
                roleNames,
                direction,
                false,
                systemUsername);

        List<User> users = toUserList(result.getRecords());
        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(users);

        List<Member> members = users.stream()
                .map(user -> convertToEntity(user, roleIdToNameMap))
                .collect(Collectors.toList());

        return new PageImpl<>(members, pageable, result.getTotal());
    }

    @Override
    public Optional<Member> findById(Long id) {
        User user = toUser(userMapper.selectById(id));
        if (user == null || user.getDisable() || user.getRoleId() == null
                || systemUsername.equals(user.getUsername())) {
            return Optional.empty();
        }

        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(List.of(user));
        return Optional.of(convertToEntity(user, roleIdToNameMap));
    }

    @Override
    public List<Member> findDirectionLeaders() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDO> mybatisPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, Integer.MAX_VALUE);

        List<String> roleNames = List.of(
                RoleType.DIRECTION_ADMIN.getName(),
                RoleType.SUPER_ADMIN.getName());

        com.baomidou.mybatisplus.core.metadata.IPage<UserDO> result = userMapper.selectByRoleNamesAndDirection(
                mybatisPage,
                roleNames,
                null,
                true,
                systemUsername);

        List<User> users = toUserList(result.getRecords());
        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(users);

        return users.stream()
                .map(user -> convertToEntity(user, roleIdToNameMap))
                .collect(Collectors.toList());
    }

    private Map<Long, String> buildRoleIdToNameMap(List<User> users) {
        List<Long> roleIds = users.stream()
                .map(User::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Role> roles = toRoleList(roleMapper.selectBatchIds(roleIds));
        return roles.stream()
                .collect(Collectors.toMap(Role::getId, Role::getName));
    }

    private Member convertToEntity(User user, Map<Long, String> roleIdToNameMap) {
        String collegeName = null;
        if (user.getCollegeId() != null) {
            College college = collegeConverter.toEntity(collegeMapper.selectById(user.getCollegeId()));
            collegeName = college != null ? college.getName() : null;
        }

        // 微信二维码（qrcode_id 直接关联 tb_file，非 tb_qrcode）
        Long wechatQrcodeFileId = user.getQrcodeId();

        Integer enrollmentYear = GradeCalculator.resolveAssessmentYear(
                user.getStudentId(),
                user.getAssessmentGradeYear());
        String roleName = user.getRoleId() != null ? roleIdToNameMap.get(user.getRoleId()) : null;

        return converter.toEntity(user, collegeName, wechatQrcodeFileId, roleName, enrollmentYear);
    }

    private User toUser(UserDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return User.reconstruct(
                dataObject.getId(),
                dataObject.getStudentId(),
                dataObject.getEmail(),
                dataObject.getRoleId(),
                dataObject.getPassword(),
                dataObject.getUsername(),
                dataObject.getNickname(),
                dataObject.getCollegeId(),
                dataObject.getMajor(),
                dataObject.getAssessmentGradeYear(),
                dataObject.getDirection(),
                dataObject.getGender(),
                dataObject.getJob(),
                dataObject.getAvatarId(),
                dataObject.getDisable(),
                dataObject.getQrcodeId(),
                dataObject.getGithubId(),
                dataObject.getGithubUsername(),
                dataObject.getInternalReferralCode(),
                dataObject.getBio());
    }

    private List<User> toUserList(List<UserDO> dataObjects) {
        if (dataObjects == null) {
            return Collections.emptyList();
        }
        return dataObjects.stream()
                .map(this::toUser)
                .toList();
    }

    private Role toRole(RoleDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return Role.reconstruct(dataObject.getId(), dataObject.getName());
    }

    private List<Role> toRoleList(List<RoleDO> dataObjects) {
        if (dataObjects == null) {
            return Collections.emptyList();
        }
        return dataObjects.stream()
                .map(this::toRole)
                .toList();
    }
}
