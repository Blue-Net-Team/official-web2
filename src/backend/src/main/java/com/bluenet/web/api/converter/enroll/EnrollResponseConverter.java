package com.bluenet.web.api.converter.enroll;

import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.application.EnrollResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 报名响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class EnrollResponseConverter {

    public EnrollmentResultDTO toEnrollmentResultDTO(EnrollResult.Enrollment result) {
        return EnrollmentResultDTO.builder()
                .id(result.id())
                .username(result.username())
                .studentId(result.studentId())
                .email(result.email())
                .collegeName(result.collegeName())
                .major(result.major())
                .gender(result.gender())
                .direction(result.direction())
                .status(result.status())
                .avatarFileId(result.avatarFileId())
                .created(result.created())
                .build();
    }

    public EnrollmentBriefDTO toBriefDTO(EnrollResult.Brief result) {
        return EnrollmentBriefDTO.builder()
                .id(result.id())
                .username(result.username())
                .studentId(result.studentId())
                .email(result.email())
                .collegeName(result.collegeName())
                .major(result.major())
                .gender(result.gender())
                .direction(result.direction())
                .status(result.status())
                .avatarFileId(result.avatarFileId())
                .build();
    }

    public List<EnrollmentBriefDTO> toBriefDTOList(List<EnrollResult.Brief> results) {
        return results.stream()
                .map(this::toBriefDTO)
                .toList();
    }

    public Page<EnrollmentBriefDTO> toBriefDTOPage(Page<EnrollResult.Brief> page) {
        return page.map(this::toBriefDTO);
    }

    public EnrollmentDetailDTO toDetailDTO(EnrollResult.Detail result) {
        return EnrollmentDetailDTO.builder()
                .id(result.id())
                .username(result.username())
                .studentId(result.studentId())
                .email(result.email())
                .collegeId(result.collegeId())
                .collegeName(result.collegeName())
                .major(result.major())
                .gender(result.gender())
                .direction(result.direction())
                .status(result.status())
                .avatarFileId(result.avatarFileId())
                .introduction(result.introduction())
                .internalReferralCode(result.internalReferralCode())
                .referralUserName(result.referralUserName())
                .build();
    }

    public EnrollmentApprovalResultDTO toApprovalDTO(EnrollResult.Approval result) {
        return EnrollmentApprovalResultDTO.builder()
                .id(result.id())
                .status(result.status())
                .createdUserId(result.createdUserId())
                .build();
    }

    public EnrollmentStatisticsDTO toStatisticsDTO(EnrollResult.Statistics result) {
        return EnrollmentStatisticsDTO.builder()
                .total(result.total())
                .byStatus(result.byStatus())
                .byDirection(result.byDirection())
                .build();
    }
}
