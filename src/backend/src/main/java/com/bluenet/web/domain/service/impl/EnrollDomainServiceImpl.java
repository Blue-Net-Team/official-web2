package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.EnrollDomainService;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import com.bluenet.web.domain.model.enumerate.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String randomPassword = generateRandomPassword(10);

        EnrollVO newEnrollment = EnrollVO.builder()
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .grade(enrollment.getGrade())
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

        EnrollStatus newStatus = existing.getStatus();

        // 新密码
        String newPassword = generateRandomPassword(10);

        EnrollVO updatedEnrollment = EnrollVO.builder()
                .id(enrollment.getId())
                .username(enrollment.getUsername())
                .studentId(enrollment.getStudentId())
                .collegeId(enrollment.getCollegeId())
                .collegeName(enrollment.getCollegeName())
                .major(enrollment.getMajor())
                .grade(enrollment.getGrade())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(newStatus)
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
    public void approveEnrollment(Long id) {
        EnrollVO enrollment = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (enrollment.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("只能审核待审核状态的报名");
        }

        Optional<UserVO> existingUser = userRepository.findByStudentId(enrollment.getStudentId());
        Long createdUserId = null;

        if (existingUser.isEmpty()) {
            createdUserId = createUserFromEnrollment(enrollment);
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
                .grade(enrollment.getGrade())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(EnrollStatus.APPROVED)
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .build();
        enrollRepository.update(approvedEnrollment);

        log.info("报名 {} 已通过审核，创建用户ID: {}", id, createdUserId);
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
                .grade(enrollment.getGrade())
                .direction(enrollment.getDirection())
                .avatarFileId(enrollment.getAvatarFileId())
                .status(EnrollStatus.REJECTED)
                .internalReferralCode(enrollment.getInternalReferralCode())
                .referralUserId(enrollment.getReferralUserId())
                .referralUserName(enrollment.getReferralUserName())
                .build();
        enrollRepository.update(rejectedEnrollment);

        log.info("报名 {} 已被拒绝，原因: {}", id, reason);
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

    private Long createUserFromEnrollment(EnrollVO enrollment) {
        // 审核通过后创建的用户应为考生角色，考核通过后才能升级为正式成员
        RoleVO candidateRole = roleRepository.findByName(RoleType.CANDIDATE.getName())
                .orElseThrow(() -> new GlobalException("CANDIDATE 角色不存在，请先初始化角色数据"));

        String referralCode = referralCodeGenerator.generate();

        User user = User.builder()
                .studentId(enrollment.getStudentId())
                .username(enrollment.getUsername())
                .collegeId(enrollment.getCollegeId())
                .major(enrollment.getMajor())
                .direction(enrollment.getDirection())
                .avatarId(enrollment.getAvatarFileId())
                .roleId(candidateRole.getId())
                .disable(false)
                .internalReferralCode(referralCode)
                .build();

        userRepository.save(user);
        log.info("创建新用户: {}, 学号: {}, 内推码: {}", user.getId(), user.getStudentId(), referralCode);

        return user.getId();
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
}
