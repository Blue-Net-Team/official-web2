package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.UserOnboardingCreateUserRequest;
import com.bluenet.web.domain.model.vo.UserOnboardingResult;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import com.bluenet.web.domain.service.UserOnboardingService;
import com.bluenet.web.domain.util.HashUtils;
import com.bluenet.web.domain.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户入职领域服务实现。
 * <p>
 * 封装"创建新用户并发放初始凭据"的完整流程， 供 WPS 表单和报名审批等多个入口复用。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOnboardingServiceImpl implements UserOnboardingService {

    private static final int PASSWORD_LENGTH = 10;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserOnboardingResult createUserWithGeneratedPassword(UserOnboardingCreateUserRequest request) {
        String initialPassword = PasswordGenerator.generate(PASSWORD_LENGTH, true);
        return createUser(request, initialPassword);
    }

    @Override
    @Transactional
    public UserOnboardingResult createUser(UserOnboardingCreateUserRequest request, String initialPassword) {
        // 检查学号是否已存在
        Optional<User> existingStudent = userRepository.findByStudentId(request.studentId());
        if (existingStudent.isPresent()) {
            throw new DataConflict("学号 " + request.studentId() + " 对应的用户已存在");
        }

        // 检查邮箱是否已存在
        Optional<User> existingEmail = userRepository.findByEmail(request.email());
        if (existingEmail.isPresent()) {
            throw new DataConflict("邮箱 " + request.email() + " 对应的用户已存在");
        }

        String hashedPassword = HashUtils.sha256(initialPassword);
        String encodedPassword = passwordEncoder.encode(hashedPassword);
        String referralCode = referralCodeGenerator.generate();

        User user = User.create(
                request.studentId(),
                request.email(),
                request.roleId(),
                encodedPassword,
                request.username(),
                null,
                request.collegeId(),
                request.major(),
                request.assessmentGradeYear(),
                request.direction(),
                request.gender() != null ? request.gender() : Gender.UNKNOWN,
                null,
                request.avatarId(),
                null,
                null,
                null,
                referralCode,
                null);

        userRepository.save(user);
        log.info(
                "用户创建成功: userId={}, studentId={}, referralCode={}",
                user.getId(),
                user.getStudentId(),
                referralCode);

        return new UserOnboardingResult(user.getId(), initialPassword, referralCode);
    }

    @Override
    public Role getMemberRole() {
        return roleRepository.findByName(RoleType.MEMBER.getName())
                .orElseThrow(() -> new IllegalStateException("MEMBER 角色不存在，请先初始化角色数据"));
    }

    @Override
    public Role getCandidateRole() {
        return roleRepository.findByName(RoleType.CANDIDATE.getName())
                .orElseThrow(() -> new IllegalStateException("CANDIDATE 角色不存在，请先初始化角色数据"));
    }
}
