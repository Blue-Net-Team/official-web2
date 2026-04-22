package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.model.vo.EnrollmentApprovalVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.EnrollDomainService;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollDomainServiceImpl implements EnrollDomainService {
    private final EnrollRepository enrollRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final RoleRepository roleRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    private static final int ENROLL_PASSWORD_LENGTH = 10;
    private static final int APPROVAL_INITIAL_PASSWORD_LENGTH = 8;

    @Override
    public Optional<EnrollVO> getEnrollmentById(Long id) {
        return enrollRepository.findById(id);
    }

    @Override
    public Optional<EnrollVO> getEnrollmentByStudentId(String studentId) {
        return enrollRepository.findByStudentId(studentId);
    }

    @Override
    public boolean existsByStudentId(String studentId) {
        return enrollRepository.existsByStudentId(studentId);
    }

    @Override
    @Transactional
    public Long createEnrollment(EnrollVO enrollment) {
        String randomPassword = generateRandomPassword(ENROLL_PASSWORD_LENGTH);

        EnrollVO newEnrollment = EnrollVO.builder()
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .gender(enrollment.getGender())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(EnrollStatus.PENDING)
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .email(enrollment.getEmail())
                .introduction(enrollment.getIntroduction())
                .password(randomPassword)
                .build();
        return enrollRepository.save(newEnrollment);
    }

    @Override
    @Transactional
    public void updateEnrollment(EnrollVO enrollment) {
        EnrollVO existing = enrollRepository.findById(enrollment.getId())
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (existing.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("报名已审核，无法更新报名信息");
        }

        String newPassword = generateRandomPassword(ENROLL_PASSWORD_LENGTH);

        EnrollVO updatedEnrollment = EnrollVO.builder()
                .id(enrollment.getId())
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .gender(enrollment.getGender())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(existing.getStatus())
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .email(enrollment.getEmail())
                .introduction(enrollment.getIntroduction())
                .password(newPassword)
                .build();

        enrollRepository.update(updatedEnrollment);
    }

    @Override
    public Page<EnrollBriefVO> getEnrollmentList(String keyword, EnrollStatus status, Direction direction,
            Pageable pageable) {
        return enrollRepository.search(keyword, status, direction, pageable);
    }

    @Override
    public EnrollStatisticsVO getStatistics() {
        return enrollRepository.getStatistics();
    }

    @Override
    @Transactional
    public EnrollmentApprovalVO approveEnrollment(Long id) {
        return approveEnrollment(id, null);
    }

    @Override
    @Transactional
    public EnrollmentApprovalVO approveEnrollment(Long id, Integer assessmentGradeYear) {
        EnrollVO enrollment = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (enrollment.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("只能审核待审核状态的报名");
        }

        Optional<UserVO> existingUser = userRepository.findByStudentId(enrollment.getStudentId());
        Long createdUserId;
        String initialPassword = null;
        boolean newUserCreated = false;

        if (existingUser.isEmpty()) {
            CreatedUserCredential credential = createUserFromEnrollment(enrollment, assessmentGradeYear);
            createdUserId = credential.userId();
            initialPassword = credential.initialPassword();
            newUserCreated = true;
        } else {
            log.info("学号 {} 对应的用户已存在，跳过创建", enrollment.getStudentId());
            createdUserId = existingUser.get().getId();
        }

        EnrollVO approvedEnrollment = EnrollVO.builder()
                .id(enrollment.getId())
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .gender(enrollment.getGender())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(EnrollStatus.APPROVED)
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .build();
        enrollRepository.update(approvedEnrollment);

        log.info("报名 {} 已通过审核，用户ID: {}", id, createdUserId);
        return EnrollmentApprovalVO.builder()
                .id(id)
                .status(EnrollStatus.APPROVED)
                .userId(createdUserId)
                .newUserCreated(newUserCreated)
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .email(enrollment.getEmail())
                .initialPassword(initialPassword)
                .build();
    }

    @Override
    @Transactional
    public void rejectEnrollment(Long id, String reason) {
        EnrollVO enrollment = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (enrollment.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("只能审核待审核状态的报名");
        }

        EnrollVO rejectedEnrollment = EnrollVO.builder()
                .id(enrollment.getId())
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .gender(enrollment.getGender())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(EnrollStatus.REJECTED)
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .build();
        enrollRepository.update(rejectedEnrollment);

        log.info("报名 {} 已拒绝，原因: {}", id, reason);
    }

    @Override
    public void validateAvatar(Long avatarId) {
        if (avatarId == null) {
            return;
        }

        FileVO file = fileRepository.findById(avatarId)
                .orElseThrow(() -> new BadRequest("头像文件不存在"));
        if (file.getType() != FileType.AVATAR) {
            throw new GlobalException("文件类型不是头像");
        }
    }

    private CreatedUserCredential createUserFromEnrollment(EnrollVO enrollment, Integer assessmentGradeYear) {
        RoleVO candidateRole = roleRepository.findByName(RoleType.CANDIDATE.getName())
                .orElseThrow(() -> new GlobalException("CANDIDATE 角色不存在，请先初始化角色数据"));

        String initialPassword = generateRandomPassword(APPROVAL_INITIAL_PASSWORD_LENGTH);
        String hashedPassword = sha256Hash(initialPassword);
        String encodedPassword = passwordEncoder.encode(hashedPassword);
        String referralCode = referralCodeGenerator.generate();

        User user = User.builder()
                .studentId(enrollment.getStudentId())
                .email(enrollment.getEmail())
                .password(encodedPassword)
                .username(enrollment.getUsername())
                .collegeId(enrollment.getCollegeId())
                .major(enrollment.getMajor())
                .assessmentGradeYear(assessmentGradeYear)
                .gender(enrollment.getGender() != null ? enrollment.getGender() : Gender.UNKNOWN)
                .direction(enrollment.getDirection())
                .avatarId(enrollment.getAvatarFileId())
                .roleId(candidateRole.getId())
                .disable(false)
                .internalReferralCode(referralCode)
                .build();

        userRepository.save(user);
        log.info("创建新用户 {}, 学号: {}, 内推码: {}", user.getId(), user.getStudentId(), referralCode);
        return new CreatedUserCredential(user.getId(), initialPassword);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 新建账号的初始凭据结果，由应用层决定是否分发给用户。
     */
    private record CreatedUserCredential(Long userId, String initialPassword) {
    }
}
