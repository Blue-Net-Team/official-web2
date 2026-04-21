package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.MemberVO;
import com.bluenet.web.domain.repository.MemberRepository;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.util.GradeCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class MemberRepositoryImpl implements MemberRepository {
    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final QrcodeMapper qrcodeMapper;
    private final RoleMapper roleMapper;
    private final String systemUsername;

    public MemberRepositoryImpl(
            UserMapper userMapper,
            CollegeMapper collegeMapper,
            FileMapper fileMapper,
            QrcodeMapper qrcodeMapper,
            RoleMapper roleMapper,
            @Value("${system-user.username}") String systemUsername) {
        this.userMapper = userMapper;
        this.collegeMapper = collegeMapper;
        this.fileMapper = fileMapper;
        this.qrcodeMapper = qrcodeMapper;
        this.roleMapper = roleMapper;
        this.systemUsername = systemUsername;
    }

    /**
     * 查询全部成员 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的成员 结果。
     */
    @Override
    public org.springframework.data.domain.Page<MemberVO> findAll(Direction direction,
            org.springframework.data.domain.Pageable pageable) {
        Page<UserDO> mybatisPage = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());

        List<String> roleNames = List.of(
                RoleType.MEMBER.getName(),
                RoleType.DIRECTION_ADMIN.getName(),
                RoleType.SUPER_ADMIN.getName());

        IPage<UserDO> result = userMapper.selectByRoleNamesAndDirection(
                mybatisPage,
                roleNames,
                direction,
                false,
                systemUsername);

        // 批量查询角色
        List<User> users = RepositoryObjectConverter.toDomainList(result.getRecords(), User.class);
        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(users);

        List<MemberVO> members = users
                .stream()
                .map(user -> convertToVO(user, roleIdToNameMap))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                members,
                pageable,
                result.getTotal());
    }

    /**
     * 按主键查询成员 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的成员 结果；不存在时为空。
     */
    @Override
    public Optional<MemberVO> findById(Long id) {
        User user = RepositoryObjectConverter.toDomain(userMapper.selectById(id), User.class);
        if (user == null || user.getDisable() || user.getRoleId() == null
                || systemUsername.equals(user.getUsername())) {
            return Optional.empty();
        }

        // 查询单个用户的角色
        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(List.of(user));
        return Optional.of(convertToVO(user, roleIdToNameMap));
    }

    /**
     * 查询各技术方向负责人成员列表。
     *
     * @return 满足条件的成员 结果集合。
     */
    @Override
    public List<MemberVO> findDirectionLeaders() {
        Page<UserDO> mybatisPage = new Page<>(1, Integer.MAX_VALUE);

        List<String> roleNames = List.of(
                RoleType.DIRECTION_ADMIN.getName(),
                RoleType.SUPER_ADMIN.getName());

        IPage<UserDO> result = userMapper.selectByRoleNamesAndDirection(
                mybatisPage,
                roleNames,
                null,
                true,
                systemUsername);

        // 批量查询角色
        List<User> users = RepositoryObjectConverter.toDomainList(result.getRecords(), User.class);
        Map<Long, String> roleIdToNameMap = buildRoleIdToNameMap(users);

        return users
                .stream()
                .map(user -> convertToVO(user, roleIdToNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 处理成员 仓储职责中的业务数据访问逻辑。
     *
     * @param users
     *            用户数据行集合。
     * @return 转换后的目标模型对象。
     */
    private Map<Long, String> buildRoleIdToNameMap(List<User> users) {
        List<Long> roleIds = users.stream()
                .map(User::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Role> roles = RepositoryObjectConverter.toDomainList(roleMapper.selectBatchIds(roleIds), Role.class);
        return roles.stream()
                .collect(Collectors.toMap(Role::getId, Role::getName));
    }

    /**
     * 在成员 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param user
     *            用户领域对象。
     * @param roleIdToNameMap
     *            角色主键到角色名称的映射。
     * @return 转换后的目标模型对象。
     */
    private MemberVO convertToVO(User user, Map<Long, String> roleIdToNameMap) {
        String collegeName = null;
        if (user.getCollegeId() != null) {
            College college = RepositoryObjectConverter
                    .toDomain(collegeMapper.selectById(user.getCollegeId()), College.class);
            collegeName = college != null ? college.getName() : null;
        }

        String wechatQrCodeUrl = null;
        if (user.getQrcodeId() != null) {
            Qrcode qrcode = RepositoryObjectConverter
                    .toDomain(qrcodeMapper.selectById(user.getQrcodeId()), Qrcode.class);
            if (qrcode != null && qrcode.getFileId() != null) {
                File wechatQrCode = RepositoryObjectConverter
                        .toDomain(fileMapper.selectById(qrcode.getFileId()), File.class);
                wechatQrCodeUrl = wechatQrCode != null ? wechatQrCode.getUrl() : null;
            }
        }

        Integer enrollmentYear = GradeCalculator.resolveAssessmentYear(
                user.getStudentId(),
                user.getAssessmentGradeYear());
        String roleName = user.getRoleId() != null ? roleIdToNameMap.get(user.getRoleId()) : null;

        RoleType roleType = roleName != null ? RoleType.fromName(roleName) : null;

        return MemberVO.builder()
                .id(user.getId())
                .studentId(user.getStudentId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .direction(user.getDirection())
                .job(user.getJob())
                .avatarFileId(user.getAvatarId())
                .college(collegeName)
                .major(user.getMajor())
                .gender(user.getGender())
                .role(roleType)
                .bio(user.getBio())
                .githubUsername(user.getGithubUsername())
                .wechatQrcode(wechatQrCodeUrl)
                .enrollmentYear(enrollmentYear)
                .assessmentGradeYear(user.getAssessmentGradeYear())
                .roleName(roleName)
                .build();
    }
}
