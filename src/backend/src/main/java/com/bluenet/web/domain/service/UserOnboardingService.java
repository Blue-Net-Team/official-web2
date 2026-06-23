package com.bluenet.web.domain.service;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.util.HashUtils;
import com.bluenet.web.domain.util.PasswordGenerator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户入职领域服务。
 * <p>
 * 封装"创建新用户并发放初始凭据"的完整流程，
 * 供 WPS 表单和报名审批等多个入口复用。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOnboardingService {

    private static final int PASSWORD_LENGTH = 10;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户，系统自动生成初始密码。
     *
     * @param request 创建用户请求
     * @return 创建结果（含生成的初始密码）
     */
    @Transactional
    public UserOnboardingResult createUserWithGeneratedPassword(CreateUserRequest request) {
        String initialPassword = PasswordGenerator.generate(PASSWORD_LENGTH, true);
        return createUser(request, initialPassword);
    }

    /**
     * 创建用户，使用外部提供的初始密码。
     *
     * @param request         创建用户请求
     * @param initialPassword 初始密码（明文）
     * @return 创建结果
     */
    @Transactional
    public UserOnboardingResult createUser(CreateUserRequest request, String initialPassword) {
        // 检查学号是否已存在
        var existingStudent = userRepository.findByStudentId(request.studentId());
        if (existingStudent.isPresent()) {
            throw new DataConflict("学号 " + request.studentId() + " 对应的用户已存在");
        }

        // 检查邮箱是否已存在
        var existingEmail = userRepository.findByEmail(request.email());
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
        log.info("用户创建成功: userId={}, studentId={}, referralCode={}",
                user.getId(), user.getStudentId(), referralCode);

        return new UserOnboardingResult(user.getId(), initialPassword, referralCode);
    }

    /**
     * 查找 MEMBER 角色，失败时抛出异常。
     */
    public RoleVO getMemberRole() {
        return roleRepository.findByName(RoleType.MEMBER.getName())
                .orElseThrow(() -> new IllegalStateException("MEMBER 角色不存在，请先初始化角色数据"));
    }

    /**
     * 查找 CANDIDATE 角色，失败时抛出异常。
     */
    public RoleVO getCandidateRole() {
        return roleRepository.findByName(RoleType.CANDIDATE.getName())
                .orElseThrow(() -> new IllegalStateException("CANDIDATE 角色不存在，请先初始化角色数据"));
    }

    /**
     * 创建用户请求参数。
     */
    @Builder
    public record CreateUserRequest(
            String studentId,
            String email,
            Long roleId,
            String username,
            Long collegeId,
            String major,
            Integer assessmentGradeYear,
            Direction direction,
            Gender gender,
            Long avatarId) {
    }

    /**
     * 创建用户结果。
     */
    @Builder
    public record UserOnboardingResult(
            Long userId,
            String initialPassword,
            String referralCode) {
    }
}
