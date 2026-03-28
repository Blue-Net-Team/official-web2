package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.domain.model.vo.EnrollVO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface EnrollService {
    EnrollmentResultDTO createEnrollment(CreateEnrollmentRequestDTO request);
    EnrollmentBriefDTO updateEnrollment(String studentId, CreateEnrollmentRequestDTO request);
    Page<EnrollmentBriefDTO> getEnrollmentList(EnrollmentListQueryDTO query);
    EnrollmentDetailDTO getEnrollmentDetail(Long id);
    EnrollmentApprovalResultDTO approveEnrollment(Long id);
    EnrollmentApprovalResultDTO rejectEnrollment(Long id, RejectEnrollmentRequestDTO request);
    EnrollmentStatisticsDTO getStatistics();
    Optional<EnrollVO> checkEnrollmentExists(String studentId);
}
