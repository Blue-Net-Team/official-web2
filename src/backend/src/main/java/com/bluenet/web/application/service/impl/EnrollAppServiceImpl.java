package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.EnrollResult;
import com.bluenet.web.application.command.enroll.EnrollCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.service.EnrollAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * 报名应用服务实现。
 * <p>
 * 实现报名聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollAppServiceImpl implements EnrollAppService {

    private final EnrollRepository enrollRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final RoleRepository roleRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final MessageDispatcher messageDispatcher;
    private final com.bluenet.web.application.message.template.EnrollmentApprovalCredentialTemplate enrollmentApprovalCredentialTemplate;
    private final com.bluenet.web.application.message.template.EnrollmentRejectionTemplate enrollmentRejectionTemplate;

    private static final int ENROLL_PASSWORD_LENGTH = 10;
    private static final int APPROVAL_INITIAL_PASSWORD_LENGTH = 8;
    private static final String APPROVAL_EMAIL_SUBJECT = "蓝网报名审核通过通知";
    private static final String REJECTION_EMAIL_SUBJECT = "蓝网报名审核未通过通知";

    /**
     * 创建报名。
     *
     * @param command
     *            创建报名命令
     * @return 报名结果
     */
    @Override
    @Transactional
    public EnrollResult.Enrollment createEnrollment(EnrollCommands.CreateEnrollmentCommand command) {
        Optional<Enroll> existing = enrollRepository.findByStudentId(command.studentId());
        if (existing.isPresent()) {
            if (Boolean.TRUE.equals(command.forceUpdate())) {
                EnrollResult.Enrollment updated = updateEnrollment(
                        new EnrollCommands.UpdateEnrollmentCommand(
                                command.studentId(),
                                command.username(),
                                command.email(),
                                command.collegeId(),
                                command.major(),
                                command.gender(),
                                command.direction(),
                                command.avatarId(),
                                command.introduction(),
                                command.internalReferralCode()));
                return new EnrollResult.Enrollment(
                        updated.id(), updated.username(), updated.studentId(), updated.email(),
                        updated.collegeId(), updated.collegeName(), updated.major(), updated.gender(),
                        updated.direction(), updated.avatarFileId(), updated.status(), updated.introduction(),
                        updated.internalReferralCode(), updated.referralUserId(), updated.referralUserName(),
                        false);
            }
            throw new DataConflict(
                    "学号已存在，是否更新报名信息？",
                    convertToConflictDTO(existing.get()));
        }

        if (command.avatarId() != null) {
            validateAvatar(command.avatarId());
        }

        String password = generateRandomPassword(ENROLL_PASSWORD_LENGTH);
        Enroll enroll = Enroll.create(
                command.username(),
                command.studentId(),
                password,
                command.internalReferralCode(),
                command.collegeId(),
                command.major(),
                command.gender(),
                command.direction(),
                command.avatarId(),
                command.email(),
                command.introduction());
        enrollRepository.save(enroll);
        return toEnrollmentResult(enroll, true);
    }

    /**
     * 更新报名。
     *
     * @param command
     *            更新报名命令
     * @return 更新后的报名结果
     */
    @Override
    @Transactional
    public EnrollResult.Enrollment updateEnrollment(EnrollCommands.UpdateEnrollmentCommand command) {
        Enroll existing = enrollRepository.findByStudentId(command.studentId())
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (existing.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("报名已审核，无法更新报名信息");
        }

        if (command.avatarId() != null) {
            validateAvatar(command.avatarId());
        }

        String newPassword = generateRandomPassword(ENROLL_PASSWORD_LENGTH);
        existing.updateInfo(
                command.username(),
                command.studentId(),
                command.collegeId(),
                command.major(),
                command.gender(),
                command.direction(),
                command.avatarId(),
                command.email(),
                command.introduction(),
                command.internalReferralCode(),
                newPassword);
        enrollRepository.update(existing);
        return toEnrollmentResult(existing, false);
    }

    /**
     * 查询报名列表。
     *
     * @param command
     *            查询报名列表命令
     * @return 报名简要信息分页结果
     */
    @Override
    public Page<EnrollResult.Brief> getEnrollmentList(EnrollCommands.GetEnrollmentListCommand command) {
        int page = command.page() != null ? command.page() : 0;
        int size = command.size() != null ? Math.min(command.size(), 100) : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<Enroll> enrollPage = enrollRepository.search(
                command.keyword(),
                command.status(),
                command.direction(),
                pageable);

        return enrollPage.map(this::toBriefResult);
    }

    /**
     * 根据ID查询报名详情。
     *
     * @param id
     *            报名ID
     * @return 报名详情结果
     */
    @Override
    public EnrollResult.Detail getEnrollmentDetail(Long id) {
        Enroll enroll = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));
        return toDetailResult(enroll);
    }

    /**
     * 审批通过报名。
     *
     * @param id
     *            报名ID
     * @return 审批结果
     */
    @Override
    @Transactional
    public EnrollResult.Approval approveEnrollment(Long id) {
        return approveEnrollment(id, new EnrollCommands.ApproveEnrollmentCommand(null));
    }

    /**
     * 审批通过报名。
     *
     * @param id
     *            报名ID
     * @param command
     *            审批报名命令
     * @return 审批结果
     */
    @Override
    @Transactional
    public EnrollResult.Approval approveEnrollment(Long id, EnrollCommands.ApproveEnrollmentCommand command) {
        Enroll enroll = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (enroll.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("只能审核待审核状态的报名");
        }

        Integer assessmentGradeYear = command.assessmentGradeYear();
        validateAssessmentGradeYear(assessmentGradeYear);

        Optional<UserVO> existingUser = userRepository.findByStudentId(enroll.getStudentId());
        Long createdUserId = null;
        String initialPassword = null;
        boolean newUserCreated = false;

        if (existingUser.isEmpty()) {
            CreatedUserCredential credential = createUserFromEnrollment(enroll, assessmentGradeYear);
            createdUserId = credential.userId();
            initialPassword = credential.initialPassword();
            newUserCreated = true;
        } else {
            log.info("学号 {} 对应的用户已存在，跳过创建", enroll.getStudentId());
            createdUserId = existingUser.get().getId();
        }

        enroll.approve();
        enrollRepository.update(enroll);

        if (newUserCreated) {
            sendApprovalCredentialMessage(enroll, initialPassword);
        }

        log.info("报名 {} 已通过审核，用户ID: {}", id, createdUserId);
        return new EnrollResult.Approval(id, EnrollStatus.APPROVED, createdUserId);
    }

    /**
     * 拒绝报名。
     *
     * @param id
     *            报名ID
     * @param command
     *            拒绝报名命令
     * @return 审批结果
     */
    @Override
    @Transactional
    public EnrollResult.Approval rejectEnrollment(Long id, EnrollCommands.RejectEnrollmentCommand command) {
        Enroll enroll = enrollRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (enroll.getStatus() != EnrollStatus.PENDING) {
            throw new DataConflict("只能处理待审核状态的报名");
        }

        enroll.reject();
        enrollRepository.update(enroll);

        sendRejectionMessage(enroll, command.reason());

        log.info("报名 {} 已拒绝，原因: {}", id, command.reason());
        return new EnrollResult.Approval(id, EnrollStatus.REJECTED, null);
    }

    /**
     * 获取统计信息。
     *
     * @return 报名统计结果
     */
    @Override
    public EnrollResult.Statistics getStatistics() {
        EnrollStatisticsVO statistics = enrollRepository.getStatistics();
        return new EnrollResult.Statistics(statistics.getTotal(), statistics.getByStatus(),
                statistics.getByDirection());
    }

    private void validateAssessmentGradeYear(Integer assessmentGradeYear) {
        if (assessmentGradeYear == null) {
            return;
        }
        if (assessmentGradeYear < 2000 || assessmentGradeYear > 2100) {
            throw new BadRequest("assessmentGradeYear must be between 2000 and 2100");
        }
    }

    private void validateAvatar(Long avatarId) {
        if (avatarId == null) {
            return;
        }
        File file = fileRepository.findById(avatarId)
                .orElseThrow(() -> new BadRequest("头像文件不存在"));
        if (file.getType() != FileType.AVATAR) {
            throw new GlobalException("文件类型不是头像");
        }
    }

    private CreatedUserCredential createUserFromEnrollment(Enroll enroll, Integer assessmentGradeYear) {
        RoleVO candidateRole = roleRepository.findByName(RoleType.CANDIDATE.getName())
                .orElseThrow(() -> new GlobalException("CANDIDATE 角色不存在，请先初始化角色数据"));

        String initialPassword = enroll.getPassword();
        String hashedPassword = sha256Hash(initialPassword);
        String encodedPassword = passwordEncoder.encode(hashedPassword);
        String referralCode = referralCodeGenerator.generate();

        User user = User.create(
                enroll.getStudentId(),
                enroll.getEmail(),
                candidateRole.getId(),
                encodedPassword,
                enroll.getUsername(),
                null,
                enroll.getCollegeId(),
                enroll.getMajor(),
                assessmentGradeYear,
                enroll.getDirection(),
                enroll.getGender() != null ? enroll.getGender() : Gender.UNKNOWN,
                null,
                enroll.getAvatarId(),
                null,
                null,
                null,
                referralCode,
                null);

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

    private void sendApprovalCredentialMessage(Enroll enroll, String initialPassword) {
        try {
            String htmlContent = enrollmentApprovalCredentialTemplate
                    .buildHtml(enroll.getUsername(), enroll.getStudentId(), initialPassword);
            messageDispatcher.dispatchAsync(
                    MessageRequest.html(MessageChannel.EMAIL, enroll.getEmail(), APPROVAL_EMAIL_SUBJECT, htmlContent));
            log.info("审核通过初始凭据消息已触发异步分发 - enrollmentId={}, email={}", enroll.getId(), enroll.getEmail());
        } catch (Exception ex) {
            log.warn("审核通过初始凭据消息分发触发失败 - enrollmentId={}, email={}", enroll.getId(), enroll.getEmail(), ex);
        }
    }

    private void sendRejectionMessage(Enroll enroll, String reason) {
        if (enroll.getEmail() == null || enroll.getEmail().isBlank()) {
            log.warn("报名 {} 无邮箱地址，跳过发送拒绝通知", enroll.getId());
            return;
        }
        try {
            String htmlContent = enrollmentRejectionTemplate
                    .buildHtml(enroll.getUsername(), reason);
            messageDispatcher.dispatchAsync(
                    MessageRequest.html(MessageChannel.EMAIL, enroll.getEmail(), REJECTION_EMAIL_SUBJECT, htmlContent));
            log.info("审核拒绝通知消息已触发异步分发 - enrollmentId={}, email={}", enroll.getId(), enroll.getEmail());
        } catch (Exception ex) {
            log.warn("审核拒绝通知消息分发触发失败 - enrollmentId={}, email={}", enroll.getId(), enroll.getEmail(), ex);
        }
    }

    private Object convertToConflictDTO(Enroll enroll) {
        return new com.bluenet.web.api.dto.enrollment.EnrollmentConflictDTO(
                enroll.getId(),
                enroll.getUsername(),
                enroll.getStudentId(),
                enroll.getStatus(),
                enroll.getDirection());
    }

    private EnrollResult.Enrollment toEnrollmentResult(Enroll enroll, boolean created) {
        return new EnrollResult.Enrollment(
                enroll.getId(),
                enroll.getUsername(),
                enroll.getStudentId(),
                enroll.getEmail(),
                enroll.getCollegeId(),
                enroll.getCollegeName(),
                enroll.getMajor(),
                enroll.getGender(),
                enroll.getDirection(),
                enroll.getAvatarId(),
                enroll.getStatus(),
                enroll.getIntroduction(),
                enroll.getInternalReferralCode(),
                enroll.getReferralUserId(),
                enroll.getReferralUserName(),
                created);
    }

    private EnrollResult.Brief toBriefResult(Enroll enroll) {
        return new EnrollResult.Brief(
                enroll.getId(),
                enroll.getUsername(),
                enroll.getStudentId(),
                enroll.getEmail(),
                enroll.getCollegeName(),
                enroll.getMajor(),
                enroll.getGender(),
                enroll.getDirection(),
                enroll.getStatus(),
                enroll.getAvatarId());
    }

    private EnrollResult.Detail toDetailResult(Enroll enroll) {
        return new EnrollResult.Detail(
                enroll.getId(),
                enroll.getUsername(),
                enroll.getStudentId(),
                enroll.getEmail(),
                enroll.getCollegeId(),
                enroll.getCollegeName(),
                enroll.getMajor(),
                enroll.getGender(),
                enroll.getDirection(),
                enroll.getAvatarId(),
                enroll.getStatus(),
                enroll.getIntroduction(),
                enroll.getInternalReferralCode(),
                enroll.getReferralUserName());
    }

    private record CreatedUserCredential(Long userId, String initialPassword) {
    }
}
