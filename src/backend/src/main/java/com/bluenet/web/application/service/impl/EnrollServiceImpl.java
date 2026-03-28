package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.application.service.EnrollService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import com.bluenet.web.domain.service.EnrollDomainService;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public EnrollmentResultDTO createEnrollment(CreateEnrollmentRequestDTO request) {
        if (request.getAvatarId() != null) {
            enrollDomainService.validateAvatar(request.getAvatarId());
        }

        EnrollVO enrollment = EnrollVO.builder()
                .username(request.getUsername())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .collegeId(request.getCollegeId())
                .major(request.getMajor())
                .grade(request.getGrade())
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
                .direction(request.getDirection())
                .status(EnrollStatus.PENDING)
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
                .grade(request.getGrade())
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
        EnrollVO enrollment = enrollDomainService.getEnrollmentById(id)
                .orElseThrow(() -> new DataNotFound("报名记录不存在"));

        enrollDomainService.approveEnrollment(id);

        Long createdUserId = null;
        var user = userMapper.selectByStudentId(enrollment.getStudentId());
        if (user != null) {
            createdUserId = user.getId();
        }

        return EnrollmentApprovalResultDTO.builder()
                .id(id)
                .status(EnrollStatus.APPROVED)
                .createdUserId(createdUserId)
                .build();
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
                .grade(vo.getGrade())
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
                .grade(vo.getGrade())
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
                .grade(vo.getGrade())
                .direction(vo.getDirection())
                .status(vo.getStatus())
                .avatarFileId(vo.getAvatarFileId())
                .introduction(vo.getIntroduction())
                .internalReferralCode(vo.getInternalReferralCode())
                .referralUserName(vo.getReferralUserName())
                .build();
    }
}
