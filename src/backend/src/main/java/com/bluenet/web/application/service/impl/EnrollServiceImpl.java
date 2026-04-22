package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.application.message.MessageChannel;
import com.bluenet.web.application.port.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.service.EnrollService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.model.vo.EnrollmentApprovalVO;
import com.bluenet.web.domain.service.EnrollDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnrollServiceImpl implements EnrollService {
    private final EnrollDomainService enrollDomainService;
    private final MessageDispatcher messageDispatcher;

    private static final String APPROVAL_EMAIL_SUBJECT = "蓝网报名审核通过通知";

    @Override
    @Transactional
    public EnrollmentResultDTO createEnrollment(CreateEnrollmentRequestDTO request) {
        Optional<EnrollVO> existing = enrollDomainService.getEnrollmentByStudentId(request.getStudentId());
        if (existing.isPresent()) {
            if (Boolean.TRUE.equals(request.getForceUpdate())) {
                EnrollmentBriefDTO updated = updateEnrollment(request.getStudentId(), request);
                return convertToResultDTO(updated, false);
            }
            throw new DataConflict(
                    "学号已存在，是否更新报名信息？",
                    convertToConflictDTO(existing.get()));
        }

        if (request.getAvatarId() != null) {
            enrollDomainService.validateAvatar(request.getAvatarId());
        }

        EnrollVO enrollment = EnrollVO.builder()
                .username(request.getUsername())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .collegeId(request.getCollegeId())
                .major(request.getMajor())
                .gender(request.getGender())
                .direction(request.getDirection())
                .avatarFileId(request.getAvatarId())
                .introduction(request.getIntroduction())
                .internalReferralCode(request.getInternalReferralCode())
                .build();

        Long id = enrollDomainService.createEnrollment(enrollment);

        return EnrollmentResultDTO.builder()
                .id(id)
                .username(request.getUsername())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .major(request.getMajor())
                .gender(request.getGender())
                .direction(request.getDirection())
                .status(EnrollStatus.PENDING)
                .avatarFileId(request.getAvatarId())
                .created(true)
                .build();
    }

    @Override
    @Transactional
    public EnrollmentBriefDTO updateEnrollment(String studentId, CreateEnrollmentRequestDTO request) {
        EnrollVO existing = enrollDomainService.getEnrollmentByStudentId(studentId)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        if (request.getAvatarId() != null) {
            enrollDomainService.validateAvatar(request.getAvatarId());
        }

        EnrollVO updated = EnrollVO.builder()
                .id(existing.getId())
                .username(request.getUsername())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .collegeId(request.getCollegeId())
                .major(request.getMajor())
                .gender(request.getGender())
                .direction(request.getDirection())
                .avatarFileId(request.getAvatarId())
                .introduction(request.getIntroduction())
                .internalReferralCode(request.getInternalReferralCode())
                .status(existing.getStatus())
                .build();

        enrollDomainService.updateEnrollment(updated);

        return convertToBriefDTO(updated);
    }

    @Override
    public Page<EnrollmentBriefDTO> getEnrollmentList(EnrollmentListQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : 0;
        int size = query.getSize() != null ? Math.min(query.getSize(), 100) : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<EnrollBriefVO> voPage = enrollDomainService.getEnrollmentList(
                query.getKeyword(),
                query.getStatus(),
                query.getDirection(),
                pageable);

        return voPage.map(this::convertToBriefDTO);
    }

    @Override
    public EnrollmentDetailDTO getEnrollmentDetail(Long id) {
        EnrollVO vo = enrollDomainService.getEnrollmentById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));
        return convertToDetailDTO(vo);
    }

    @Override
    @Transactional
    public EnrollmentApprovalResultDTO approveEnrollment(Long id) {
        return approveEnrollment(id, null);
    }

    @Override
    @Transactional
    public EnrollmentApprovalResultDTO approveEnrollment(Long id, ApproveEnrollmentRequestDTO request) {
        enrollDomainService.getEnrollmentById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        Integer assessmentGradeYear = request != null ? request.getAssessmentGradeYear() : null;
        validateAssessmentGradeYear(assessmentGradeYear);
        EnrollmentApprovalVO approval = enrollDomainService.approveEnrollment(id, assessmentGradeYear);
        sendApprovalCredentialMessage(approval);

        return EnrollmentApprovalResultDTO.builder()
                .id(id)
                .status(EnrollStatus.APPROVED)
                .createdUserId(approval.getUserId())
                .build();
    }

    private void validateAssessmentGradeYear(Integer assessmentGradeYear) {
        if (assessmentGradeYear == null) {
            return;
        }
        if (assessmentGradeYear < 2000 || assessmentGradeYear > 2100) {
            throw new BadRequest("assessmentGradeYear must be between 2000 and 2100");
        }
    }

    @Override
    @Transactional
    public EnrollmentApprovalResultDTO rejectEnrollment(Long id, RejectEnrollmentRequestDTO request) {
        enrollDomainService.rejectEnrollment(id, request != null ? request.getReason() : null);

        return EnrollmentApprovalResultDTO.builder()
                .id(id)
                .status(EnrollStatus.REJECTED)
                .build();
    }

    @Override
    public EnrollmentStatisticsDTO getStatistics() {
        EnrollStatisticsVO vo = enrollDomainService.getStatistics();
        return EnrollmentStatisticsDTO.builder()
                .total(vo.getTotal())
                .byStatus(vo.getByStatus())
                .byDirection(vo.getByDirection())
                .build();
    }

    @Override
    public Optional<EnrollVO> checkEnrollmentExists(String studentId) {
        return enrollDomainService.getEnrollmentByStudentId(studentId);
    }

    private EnrollmentBriefDTO convertToBriefDTO(EnrollVO vo) {
        return EnrollmentBriefDTO.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .email(vo.getEmail())
                .collegeName(vo.getCollegeName())
                .major(vo.getMajor())
                .gender(vo.getGender())
                .direction(vo.getDirection())
                .status(vo.getStatus())
                .avatarFileId(vo.getAvatarFileId())
                .build();
    }

    private EnrollmentBriefDTO convertToBriefDTO(EnrollBriefVO vo) {
        return EnrollmentBriefDTO.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .email(vo.getEmail())
                .collegeName(vo.getCollegeName())
                .major(vo.getMajor())
                .gender(vo.getGender())
                .direction(vo.getDirection())
                .status(vo.getStatus())
                .avatarFileId(vo.getAvatarFileId())
                .build();
    }

    private EnrollmentDetailDTO convertToDetailDTO(EnrollVO vo) {
        return EnrollmentDetailDTO.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .email(vo.getEmail())
                .collegeId(vo.getCollegeId())
                .collegeName(vo.getCollegeName())
                .major(vo.getMajor())
                .gender(vo.getGender())
                .direction(vo.getDirection())
                .status(vo.getStatus())
                .avatarFileId(vo.getAvatarFileId())
                .introduction(vo.getIntroduction())
                .internalReferralCode(vo.getInternalReferralCode())
                .referralUserName(vo.getReferralUserName())
                .build();
    }

    private EnrollmentResultDTO convertToResultDTO(EnrollmentBriefDTO dto, boolean created) {
        return EnrollmentResultDTO.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .studentId(dto.getStudentId())
                .email(dto.getEmail())
                .collegeName(dto.getCollegeName())
                .major(dto.getMajor())
                .gender(dto.getGender())
                .direction(dto.getDirection())
                .status(dto.getStatus())
                .avatarFileId(dto.getAvatarFileId())
                .created(created)
                .build();
    }

    private EnrollmentConflictDTO convertToConflictDTO(EnrollVO vo) {
        return EnrollmentConflictDTO.builder()
                .id(vo.getId())
                .username(vo.getUsername())
                .studentId(vo.getStudentId())
                .status(vo.getStatus())
                .direction(vo.getDirection())
                .build();
    }

    private void sendApprovalCredentialMessage(EnrollmentApprovalVO approval) {
        if (!approval.isNewUserCreated()) {
            return;
        }
        try {
            String htmlContent = buildApprovalCredentialEmailContent(approval);
            messageDispatcher.dispatchAsync(
                    MessageRequest
                            .html(MessageChannel.EMAIL, approval.getEmail(), APPROVAL_EMAIL_SUBJECT, htmlContent));
            log.info("审核通过初始凭据消息已触发异步分发 - enrollmentId={}, email={}", approval.getId(), approval.getEmail());
        } catch (Exception ex) {
            log.warn("审核通过初始凭据消息分发触发失败 - enrollmentId={}, email={}", approval.getId(), approval.getEmail(), ex);
        }
    }

    private String buildApprovalCredentialEmailContent(EnrollmentApprovalVO approval) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:640px;margin:0 auto;padding:20px;color:#333;">
                    <h2 style="color:#1f7ae0;text-align:center;">蓝网报名审核已通过</h2>
                    <p>您好，%s 同学：</p>
                    <p>您的报名申请（学号：<strong>%s</strong>）已审核通过，系统已为您创建账号。</p>
                    <div style="background:#f6f8fb;border:1px solid #e6ebf2;border-radius:8px;padding:16px;margin:16px 0;">
                        <p style="margin:0 0 8px 0;">初始登录密码：</p>
                        <p style="margin:0;font-size:22px;font-weight:bold;color:#d4380d;letter-spacing:1px;">%s</p>
                    </div>
                    <p style="margin:0 0 8px 0;">安全提示：</p>
                    <ul style="margin-top:0;padding-left:20px;">
                        <li>请在首次登录后尽快修改密码。</li>
                        <li>请勿将密码透露给他人。</li>
                    </ul>
                    <p style="color:#999;font-size:12px;">此邮件由系统自动发送，请勿直接回复。</p>
                </div>
                """
                .formatted(approval.getUsername(), approval.getStudentId(), approval.getInitialPassword());
    }
}
